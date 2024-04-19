package com.javasteam.cs;

import static com.javasteam.protobufs.Gcsystemmsgs.EGCBaseClientMsg;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.javasteam.models.StructLoader;
import com.javasteam.models.ThrowingFunction;
import com.javasteam.protobufs.GcsdkGcmessages;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum CSProtoStructLoader implements StructLoader<GeneratedMessage> {
  GC_CLIENT_WELCOME(
      EGCBaseClientMsg.k_EMsgGCClientWelcome_VALUE, GcsdkGcmessages.CMsgClientWelcome::parseFrom);

  private final int emsg;
  private final Function<byte[], GeneratedMessage> loader;

  <T extends GeneratedMessage> CSProtoStructLoader(
      int emsg, ThrowingFunction<byte[], T, InvalidProtocolBufferException> parseFrom) {
    this.emsg = emsg;
    this.loader =
        data -> {
          try {
            return parseFrom.apply(data);
          } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
          }
        };
  }
}
