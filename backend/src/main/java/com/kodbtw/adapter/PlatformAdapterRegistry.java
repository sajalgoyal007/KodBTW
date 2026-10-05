package com.kodbtw.adapter;

import com.kodbtw.entity.Platform;
import com.kodbtw.exception.UnsupportedPlatformException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformAdapterRegistry {

    private final Map<Platform, PlatformAdapter> adapterMap = new EnumMap<>(Platform.class);

    public PlatformAdapterRegistry(List<PlatformAdapter> adapters) {
        for (PlatformAdapter adapter : adapters) {
            adapterMap.put(adapter.getPlatform(), adapter);
        }
    }

    public PlatformAdapter getAdapter(Platform platform) {
        if (platform == null || !adapterMap.containsKey(platform)) {
            throw new UnsupportedPlatformException("No adapter registered for platform: " + platform);
        }
        return adapterMap.get(platform);
    }
}
