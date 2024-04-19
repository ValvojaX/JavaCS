package com.javasteam.cs;

import com.google.protobuf.InvalidProtocolBufferException;
import com.javasteam.models.AbstractMessage;
import com.javasteam.models.ProtoHeader;
import com.javasteam.models.containers.StructContainer;
import com.javasteam.protobufs.Cstrike15Gcmessages;
import com.javasteam.protobufs.Gcsystemmsgs;
import com.javasteam.steam.GameCoordinator;
import com.javasteam.steam.SteamClient;
import com.javasteam.steam.common.EPersonaState;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.javasteam.protobufs.Gcsystemmsgs.EGCBaseClientMsg;
import static com.javasteam.protobufs.GcsdkGcmessages.CMsgClientHello;
import static com.javasteam.protobufs.GcsdkGcmessages.CMsgClientWelcome;

@Slf4j
public class CSClient {
  private static final int APP_ID = 730;
  private static final int PROTOCOL_VERSION = 2000202;
  private final SteamClient steamClient;
  private final GameCoordinator gameCoordinator;

  public CSClient(SteamClient steamClient) {
    this.steamClient = steamClient;
    this.gameCoordinator = new GameCoordinator(steamClient, APP_ID);

    StructContainer.register(CSProtoStructLoader.values());
    addMessageListeners();
  }

  private void addMessageListeners() {
    steamClient.addMessageListener(
        EGCBaseClientMsg.k_EMsgGCClientWelcome_VALUE, this::onGCClientWelcome);
  }

  public void launch() {
    if (!steamClient.isConnected()) {
      throw new RuntimeException("Steam client is not connected");
    }

    steamClient.setState(EPersonaState.ONLINE);
    steamClient.setGamesPlayed(List.of(APP_ID));

    CMsgClientHello clientHello = CMsgClientHello.newBuilder().setVersion(PROTOCOL_VERSION).build();

    while (true) {
      try {
        gameCoordinator.write(Gcsystemmsgs.EGCBaseClientMsg.k_EMsgGCClientHello_VALUE, clientHello);
        gameCoordinator.waitForMessage(EGCBaseClientMsg.k_EMsgGCClientWelcome_VALUE, 5000L);
        break;
      } catch (TimeoutException ignored) {
        log.warn("Timeout while waiting for GC client welcome, retrying...");
      }
    }
  }

  private void onGCClientWelcome(AbstractMessage<ProtoHeader, CMsgClientWelcome> msg) {
    log.info("Received GC client welcome:\n{}", msg);

    var body =
        msg.getMsgBody()
            .orElseThrow(() -> new RuntimeException("Failed to parse GC client welcome"));

    try {
      onCStrike15Welcome(Cstrike15Gcmessages.CMsgCStrike15Welcome.parseFrom(body.getGameData()));
    } catch (InvalidProtocolBufferException exception) {
      log.error("Error while parsing GC client welcome", exception);
    }
  }

  private static void onCStrike15Welcome(Cstrike15Gcmessages.CMsgCStrike15Welcome msg) {
    log.info("Received cstrike15 welcome:\n{}", msg);
  }
}
