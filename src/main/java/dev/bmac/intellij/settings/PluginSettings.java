package dev.bmac.intellij.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings to hold users settings for the plugin.
 */
@Service
@State(name = "IndexExclusion", storages = @Storage("indexExclusion.xml"))
public class PluginSettings implements PersistentStateComponent<PluginSettings> {
    private boolean frontEndIndex = false;
    private boolean TODOIndex = false;
    private List<IndexExclusion> indexPathExclude = Lists.newArrayList();

    public static PluginSettings getInstance() {
        return ServiceManager.getService(PluginSettings.class);
    }

    @ApiStatus.Experimental
    public boolean isFrontEndIndexDisabled() {
        return frontEndIndex;
    }

    public void setFrontEndIndexDisabled(boolean frontEndIndex) {
        this.frontEndIndex = frontEndIndex;
    }

    @ApiStatus.Experimental
    public boolean isTODOIndexDisabled() {
        return TODOIndex;
    }

    @ApiStatus.Experimental
    public List<IndexExclusion> getIndexPathExclude()  {
        return new ArrayList<>(indexPathExclude);
    }

    public void setIndexPathExclude(List<IndexExclusion> paths) {
        this.indexPathExclude = paths;
    }

    public void setTODOIndexDisabled(boolean TODOIndex) {
        this.TODOIndex = TODOIndex;
    }
    @Nullable
    @Override
    @JsonIgnore
    public PluginSettings getState() {
        return this;
    }

    @Override
    @JsonIgnore
    public void loadState(@NotNull PluginSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
