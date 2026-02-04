package com.github.zen05.lunarsaga.asset;

import com.badlogic.gdx.assets.AssetDescriptor;

public interface Asset<T> {

    AssetDescriptor<T> getDescriptor();

}
