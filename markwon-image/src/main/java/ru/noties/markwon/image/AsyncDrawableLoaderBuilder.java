package ru.noties.markwon.image;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.noties.markwon.image.data.DataUriSchemeHandler;
import ru.noties.markwon.image.network.NetworkSchemeHandler;

class AsyncDrawableLoaderBuilder {

    ExecutorService executorService;
    final Map<String, SchemeHandler> schemeHandlers = new HashMap<>(3);
    final Map<String, MediaDecoder> mediaDecoders = new HashMap<>(3);
    MediaDecoder defaultMediaDecoder;
    ImagesPlugin.PlaceholderProvider placeholderProvider;
    ImagesPlugin.ErrorHandler errorHandler;

    boolean isBuilt;

    AsyncDrawableLoaderBuilder() {

        // @since 4.0.0-SNAPSHOT
        // okay, let's add supported schemes at the start, this would be : data-uri and default network
        // we should not use file-scheme as it's a bit complicated to assume file usage (lack of permissions)
        addSchemeHandler(DataUriSchemeHandler.create());
        addSchemeHandler(NetworkSchemeHandler.create());
    }

    void executorService(@NonNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    void addSchemeHandler(@NonNull SchemeHandler schemeHandler) {
        for (String scheme : schemeHandler.supportedSchemes()) {
            schemeHandlers.put(scheme, schemeHandler);
        }
    }

    void addMediaDecoder(@NonNull MediaDecoder mediaDecoder) {
        for (String type : mediaDecoder.supportedTypes()) {
            mediaDecoders.put(type, mediaDecoder);
        }
    }

    void defaultMediaDecoder(@Nullable MediaDecoder mediaDecoder) {
        this.defaultMediaDecoder = mediaDecoder;
    }

    void removeSchemeHandler(@NonNull String scheme) {
        schemeHandlers.remove(scheme);
    }

    void removeMediaDecoder(@NonNull String contentType) {
        mediaDecoders.remove(contentType);
    }

    /**
     * @since 3.0.0
     */
    void placeholderProvider(@NonNull ImagesPlugin.PlaceholderProvider placeholderDrawableProvider) {
        this.placeholderProvider = placeholderDrawableProvider;
    }

    /**
     * @since 3.0.0
     */
    void errorHandler(@NonNull ImagesPlugin.ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @NonNull
    AsyncDrawableLoader build() {

        isBuilt = true;

        // @since 4.0.0-SNAPSHOT
        if (defaultMediaDecoder == null) {
            defaultMediaDecoder = DefaultImageMediaDecoder.create();
        }

        if (executorService == null) {
            executorService = Executors.newCachedThreadPool();
        }

        return new AsyncDrawableLoaderImpl(this);
    }
}
