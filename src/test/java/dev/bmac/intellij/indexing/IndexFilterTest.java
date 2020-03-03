package dev.bmac.intellij.indexing;

import com.google.common.collect.Lists;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.util.indexing.ID;
import dev.bmac.intellij.settings.IndexExclusion;
import dev.bmac.intellij.settings.PluginSettings;
import org.junit.Test;

public class IndexFilterTest extends UsefulTestCase {

    @Test
    public void testExclusionSettings() {
        PluginSettings settings = new PluginSettings();
        IndexExclusion.IndexNameExclusion indexNameExclusion = new IndexExclusion.IndexNameExclusion();
        indexNameExclusion.setIndexerNames(Lists.newArrayList("a", "b"));
        indexNameExclusion.setExcludeIfNotIn(false);
        IndexExclusion exclusion = new IndexExclusion(".*", indexNameExclusion);
        settings.setIndexPathExclude(Lists.newArrayList(exclusion));
        IndexFilter filter = new IndexFilter(settings);

        VirtualFile mockFile = new MockVirtualFile("/test");
        ID a = ID.create("a");
        ID c = ID.create("c");

        assertTrue("Expect to filter out requests with any file and Id in the list",
                filter.isExcludedFromIndex(mockFile, a));
        assertFalse("Expect not to filter out requests with any file and Id not in the list",
                filter.isExcludedFromIndex(mockFile, c));

        indexNameExclusion.setExcludeIfNotIn(true);
        filter = new IndexFilter(settings);
        assertTrue("Expect to filter out request if excludeIfNotIn is set and id is not in the list",
                filter.isExcludedFromIndex(mockFile, c));
        assertFalse("Expect not to filter out request if excludeIfNotIn is set and id is in the list",
                filter.isExcludedFromIndex(mockFile, a));
    }

    @Test
    public void testExclusionPathRegex() {
        PluginSettings settings = new PluginSettings();
        IndexExclusion.IndexNameExclusion indexNameExclusion = new IndexExclusion.IndexNameExclusion();
        indexNameExclusion.setIndexerNames(Lists.newArrayList("a"));
        indexNameExclusion.setExcludeIfNotIn(false);
        IndexExclusion exclusion = new IndexExclusion(".*/test/.*", indexNameExclusion);
        settings.setIndexPathExclude(Lists.newArrayList(exclusion));
        IndexFilter filter = new IndexFilter(settings);

        ID a = ID.create("a");

        assertTrue("Expect to filter out requests with any file matching regex",
                filter.isExcludedFromIndex(new MockVirtualFile("blah/test/"), a));
        assertTrue("Expect to filter out requests with any file matching regex",
                filter.isExcludedFromIndex(new MockVirtualFile("blah/test/stuff"), a));

        assertFalse("Expect to not filter out requests with a file not matching the regex",
                filter.isExcludedFromIndex(new MockVirtualFile("some/other/path"), a));

    }

    @Test
    public void testWildcardInMiddle() {
        PluginSettings settings = new PluginSettings();
        IndexExclusion.IndexNameExclusion indexNameExclusion = new IndexExclusion.IndexNameExclusion();
        indexNameExclusion.setIndexerNames(Lists.newArrayList("a"));
        indexNameExclusion.setExcludeIfNotIn(false);
        IndexExclusion exclusion = new IndexExclusion(".*/test/.*/stuff/.*", indexNameExclusion);
        settings.setIndexPathExclude(Lists.newArrayList(exclusion));
        IndexFilter filter = new IndexFilter(settings);

        ID a = ID.create("a");

        assertFalse("Should not filter out that which does not match",
                filter.isExcludedFromIndex(new MockVirtualFile("blah/test/"), a));
        assertTrue("Should filter out since it does match",
                filter.isExcludedFromIndex(new MockVirtualFile("blah/test/test/stuff/"), a));

        assertTrue("Should filter out since it does match",
                filter.isExcludedFromIndex(new MockVirtualFile("blah/test/some/stuff/path"), a));

    }

    @Test
    public void testWildcardInMiddleOfPart() {
        PluginSettings settings = new PluginSettings();
        IndexExclusion.IndexNameExclusion indexNameExclusion = new IndexExclusion.IndexNameExclusion();
        indexNameExclusion.setIndexerNames(Lists.newArrayList("a"));
        indexNameExclusion.setExcludeIfNotIn(false);
        IndexExclusion exclusion = new IndexExclusion(".*/test.*/stuff/.*", indexNameExclusion);
        settings.setIndexPathExclude(Lists.newArrayList(exclusion));
        IndexFilter filter = new IndexFilter(settings);

        ID a = ID.create("a");

        assertFalse("Should not filter out that which does not match",
                filter.isExcludedFromIndex(new MockVirtualFile("blah/something/stuff"), a));
        assertTrue("Filter out path that matches",
                filter.isExcludedFromIndex(new MockVirtualFile("/test/stuff/"), a));

        assertTrue("Filter out path that matches",
                filter.isExcludedFromIndex(new MockVirtualFile("/testing/stuff/path"), a));

    }
}
