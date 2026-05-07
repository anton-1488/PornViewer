package com.plovdev.pornviewer.pvvasupport;

import com.plovdev.pornviewer.commons.adapter.AdapterInfo;
import com.plovdev.pornviewer.database.PVVAProvider;
import com.plovdev.pornviewer.exceptions.AdapterLoadingException;
import com.plovdev.pornviewer.utils.files.PVFileManager;
import org.jspecify.annotations.Nullable;
import org.plovdev.pvva.models.PVVAHost;
import org.plovdev.pvva.read.PVVAReader;

import java.io.IOException;
import java.nio.file.Path;

public final class PVVASupportManager {
    private PVVASupportManager() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable PVVAHost loadPvvaById(String pluginId) {
        AdapterInfo info = PVVAProvider.getAdapterById(pluginId);
        Path adapterPath = PVFileManager.getPvAdapterPath(info.pathName());
        try (PVVAReader reader = new PVVAReader(adapterPath)) {
            return reader.parseVideoAdapter();
        } catch (IOException e) {
            throw new AdapterLoadingException("Error load pvva plugin", e);
        }
    }
}