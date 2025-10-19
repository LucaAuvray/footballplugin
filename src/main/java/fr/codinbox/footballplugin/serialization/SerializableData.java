package fr.codinbox.footballplugin.serialization;

public interface SerializableData<T extends SerializedData<?>> {

    T serialize();

}
