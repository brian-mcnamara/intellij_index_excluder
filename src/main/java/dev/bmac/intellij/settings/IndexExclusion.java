package dev.bmac.intellij.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.intellij.psi.search.FilenameIndex;

import java.util.List;
import java.util.Objects;

/**
 * POJO to store a index exclusion item.
 **/
public class IndexExclusion {
    private String path;
    private IndexNameExclusion indexerNameExclusion;

    public IndexExclusion() {
        indexerNameExclusion = new IndexNameExclusion();
    }

    public IndexExclusion(String path, IndexNameExclusion indexerNameExclusion) {
        this.path = path;
        this.indexerNameExclusion = indexerNameExclusion;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IndexNameExclusion getIndexerNameExclusion() {
        return indexerNameExclusion;
    }

    public void setIndexerNameExclusion(IndexNameExclusion indexerNameExclusion) {
        this.indexerNameExclusion = indexerNameExclusion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexExclusion that = (IndexExclusion) o;
        return path.equals(that.path) &&
                indexerNameExclusion.equals(that.indexerNameExclusion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, indexerNameExclusion);
    }

    public static class IndexNameExclusion {
        private static final IndexNameExclusion DEFAULT = new IndexNameExclusion();
        private List<String> indexerNames;
        private boolean excludeIfNotIn;

        public IndexNameExclusion() {
            this.excludeIfNotIn = true;
            this.indexerNames = Lists.newArrayList(FilenameIndex.NAME.getName());
        }

        public List<String> getIndexerNames() {
            return indexerNames;
        }

        public void setIndexerNames(List<String> indexerNames) {
            this.indexerNames = indexerNames;
        }

        public boolean isExcludeIfNotIn() {
            return excludeIfNotIn;
        }

        public void setExcludeIfNotIn(boolean excludeIfNotIn) {
            this.excludeIfNotIn = excludeIfNotIn;
        }

        @JsonIgnore
        public boolean isDefault() {
            return this.equals(DEFAULT);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndexNameExclusion that = (IndexNameExclusion) o;
            return excludeIfNotIn == that.excludeIfNotIn &&
                    indexerNames.equals(that.indexerNames);
        }

        @Override
        public int hashCode() {
            return Objects.hash(indexerNames, excludeIfNotIn);
        }
    }
}
