package com.javasteam;

import com.javasteam.cs.CSClient;
import com.javasteam.steam.LoginParameters;
import com.javasteam.steam.SteamClient;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();

    SteamClient steamClient = new SteamClient();
    CSClient csClient = new CSClient(steamClient);

    steamClient.login(LoginParameters.withSessionFile(dotenv.get("STEAM_USERNAME"), dotenv.get("STEAM_PASSWORD")));
    csClient.launch();
  }
}
