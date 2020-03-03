package dev.bmac.intellij.indexing;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.indexing.GlobalIndexFilter;
import com.intellij.util.indexing.IndexId;
import dev.bmac.intellij.settings.IndexExclusion;
import dev.bmac.intellij.settings.PluginSettings;
import jregex.Pattern;
import jregex.REFlags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Extension to allow disabling indexers per index/file. Loads settings to have a user defined exclusion list.
 **/
public class IndexFilter implements GlobalIndexFilter {

    private static final com.intellij.openapi.diagnostic.Logger LOGGER = com.intellij.openapi.diagnostic.Logger.getInstance(IndexFilter.class);

    private final boolean todoDisabled;
    private final boolean frontEndIndexDisabled;
    private final java.util.List<IndexExclusion> indexExclusionList;
    private final Map<FuzzyRegex, IndexExclusion> pathExclusionPatterns;
    private final boolean enabled;
    private final com.google.common.cache.Cache<String, Optional<IndexExclusion>> indexExclusionCache = CacheBuilder.newBuilder().build();

    private long count = 0;
    private long duration = 0;

    public IndexFilter() {
        this(PluginSettings.getInstance());
    }

    IndexFilter(PluginSettings settings) {
        this.todoDisabled = settings.isTODOIndexDisabled();
        this.frontEndIndexDisabled = settings.isFrontEndIndexDisabled();
        this.indexExclusionList = settings.getIndexPathExclude();
        this.pathExclusionPatterns = indexExclusionList.stream().collect(Collectors.toMap(i -> new FuzzyRegex(i.getPath()), i -> i));
        this.enabled = todoDisabled || frontEndIndexDisabled || !indexExclusionList.isEmpty();
    }

    @Override
    public boolean isExcludedFromIndex(@NotNull com.intellij.openapi.vfs.VirtualFile virtualFile, @NotNull IndexId<?, ?> indexId) {
        if (!enabled) return false;
        if (todoDisabled && indexId.equals(com.intellij.psi.impl.cache.impl.todo.TodoIndex.NAME)) return true;
        Stopwatch sw = Stopwatch.createStarted();
        count++;
        if (frontEndIndexDisabled && isFrontend(indexId)) return true;
        try {
            Optional<IndexExclusion> indexExclusion = indexExclusionCache.get(virtualFile.getPath(), () -> {
                for (Map.Entry<FuzzyRegex, IndexExclusion> entry : pathExclusionPatterns.entrySet()) {
                    //TODO, multiple indexExclusions may apply... User beware.
                    if (entry.getKey().matches(virtualFile.getPath())) return Optional.of(entry.getValue());
                }
                return Optional.empty();
            });
            if (indexExclusion.isPresent()) {
                List<String> indexNameList = indexExclusion.get().getIndexerNameExclusion().getIndexerNames();
                if (indexExclusion.get().getIndexerNameExclusion().isExcludeIfNotIn()) {
                    if (!indexNameList.contains(indexId.getName())) {
                        sw.stop();
                        duration += sw.elapsed(TimeUnit.NANOSECONDS);
                        return true;
                    }
                } else {
                    if (indexNameList.contains(indexId.getName())) {
                        sw.stop();
                        duration += sw.elapsed(TimeUnit.NANOSECONDS);
                        return true;
                    }
                }
            }
        } catch (ExecutionException e) {
            LOGGER.error("Failed to execute cache load for index exclusion", e);
        }
        sw.stop();
        duration += sw.elapsed(TimeUnit.NANOSECONDS);
        return false;
    }

    @Override
    public int getVersion() {
        return (frontEndIndexDisabled ? 1 : 0) + (todoDisabled ? 2 : 0) + (indexExclusionList.isEmpty() ? 0 : indexExclusionList.hashCode());
    }

    @Override
    public boolean affectsIndex(@NotNull IndexId<?, ?> indexId) {
        if (!enabled) return false;
        if (!indexExclusionList.isEmpty()) return true;
        if (todoDisabled && indexId.equals(com.intellij.psi.impl.cache.impl.todo.TodoIndex.NAME)) return true;
        if (frontEndIndexDisabled && isFrontend(indexId)) return true;
        return false;
    }

    private static boolean isFrontend(IndexId<?, ?> index) {
        String name = index.getName();
        if (name.startsWith("js.") || name.startsWith("angularjs.") || name.startsWith("css.") || name.equals("CssIndex") ||
                name.startsWith("html5.") || name.equals("HtmlTagIdIndex") || name.startsWith("dom.")) return true;
        return false;
    }

    public void logStats() {
        LOGGER.info("Index filter took " + duration + "ns for " + count + " times");
    }

    public static final class IndexFilterLogStartupActivity implements StartupActivity {
        @Override
        public void runActivity(@NotNull com.intellij.openapi.project.Project project) {
            for (GlobalIndexFilter filter : IndexFilter.EP_NAME.getExtensions()) {
                if (filter instanceof IndexFilter) {
                    ((IndexFilter) filter).logStats();
                    ((IndexFilter) filter).indexExclusionCache.invalidateAll();
                    ((IndexFilter) filter).count = 0;
                    ((IndexFilter) filter).duration = 0;
                }
            }
        }
    }
    /**
     * Class to help speed up basic regular expressions.
     * Basic .*\/some/path/.* will exclude regular expressions all together.
     * Others will use fuzzyMatching to try to check for contains to a path and returns false early if there is no contains
     */
    private static class FuzzyRegex {
        private final String fuzzyMatch;
        private final Pattern pattern;
        private final boolean requiresPatternMatch;

        public FuzzyRegex(String pattern) {
            this.pattern = new Pattern(pattern, REFlags.IGNORE_CASE);
            String[] parts = pattern.split("/");
            String fuzzy = "";
            boolean previousPartSet = false;
            boolean matcherInMiddle = false;
            //Try to make the longest continuous string for fuzzy match. Does not account for a few things,
            //probably a better way out there, but this should help.
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (!(part.contains(".") || part.contains("*") || part.contains("(") || part.contains("\\"))) {
                    if (previousPartSet) {
                        fuzzy += part + ((i != parts.length - 1) ? "/" : "");
                    } else {
                        if (part.length() > fuzzy.length()) {
                            fuzzy = (i != 0 ? "/" : "") + part + ((i != parts.length - 1) ? "/" : "");
                            previousPartSet = true;
                        }
                    }
                } else {
                    previousPartSet = false;
                    if (i != 0 && i != parts.length - 1) {
                        matcherInMiddle = true;
                    }

                }
            }
            requiresPatternMatch = !(parts.length >= 3 && !matcherInMiddle &&
                    parts[0].equals(".*") && parts[parts.length - 1].equals(".*"));
            this.fuzzyMatch = fuzzy.isEmpty() ? null : fuzzy;
            assert requiresPatternMatch || fuzzyMatch != null : "Fuzzymatch should not be null if we do not require pattern match";
        }

        public boolean matches(String input) {
            if (!requiresPatternMatch) {
                return input.contains(fuzzyMatch);
            } else if (this.fuzzyMatch != null && !input.contains(fuzzyMatch)) {
                return false;
            }
            return pattern.matches(input);
        }
    }
}
