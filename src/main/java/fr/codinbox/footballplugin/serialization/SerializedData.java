package fr.codinbox.footballplugin.serialization;

public interface SerializedData<T extends SerializableData<?>> {

    T toLegacyData();

}
