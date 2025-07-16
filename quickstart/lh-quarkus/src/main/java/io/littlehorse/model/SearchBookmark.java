package io.littlehorse.model;

import com.google.protobuf.ByteString;
import java.util.Base64;

public class SearchBookmark {
    private final byte[] value;

    public SearchBookmark(byte[] value) {
        this.value = value;
    }

    public static SearchBookmark fromString(String base64Bookmark) {
        return new SearchBookmark(Base64.getDecoder().decode(base64Bookmark));
    }

    public static SearchBookmark fromProto(ByteString byteString) {
        return new SearchBookmark(byteString.toByteArray());
    }

    public ByteString toByteString() {
        return ByteString.copyFrom(this.value);
    }

    public byte[] toByteArray() {
        return this.value;
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.value);
    }
}
