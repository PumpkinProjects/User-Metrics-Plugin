package org.makingstan;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class JsonBuilder {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);


    // MonsterName, Kills
    private HashMap<String, Integer> monsterKills;
    //MonsterName, Loot
    private HashMap<String, String> monsterLoot;
    private int npcTimeTreshold;
    private ScheduledFuture<?> databaseSenderHandler = null;


    public JsonBuilder(int npcTimeTreshold)
    {
        monsterKills = new HashMap<String, Integer>();
        monsterLoot = new HashMap<String, String>();
        this.npcTimeTreshold = npcTimeTreshold;
    }



    //Add an npc kill
    public void addNPCKill(String name, String loot)
    {
        monsterKills.put(name, monsterKills.getOrDefault(name, 0)+1);
        monsterLoot.put(name, monsterLoot.getOrDefault(name, "")+loot);
    }


    //TODO
    public void addSkillAction(String skillActionName, int xp)
    {

    }

    // This method will send the variables with their respective values over to the database
    private void sendToDatabase()
    {
        Gson gson = new Gson();

        //Final document data will be the data that will be send to the server
        HashMap<String, Object> finalDocumentDataMap = new HashMap<String, Object>();

        // The combat hashmap includes all the combat data like monster kills, monster loot
        HashMap<String, HashMap> combatHashmap = new HashMap<String, HashMap>();

        //TODO
        HashMap<String, HashMap> skillingHashmap = new HashMap<String, HashMap>();

        // Abstract away the combat part and put the kills and loot in one central place
        combatHashmap.put("Kills", monsterKills);
        combatHashmap.put("Loot", monsterLoot);

        // Abstract it even more and put your combat and Skilling objects together
        finalDocumentDataMap.put("Combat", combatHashmap);
        finalDocumentDataMap.put("Skilling", skillingHashmap);

        //HARDCODED for now
        finalDocumentDataMap.put("token", System.getenv("TOKEN"));

        //Convert the data from a hashmap to a json String
        String finalDocumentData = gson.toJson(finalDocumentDataMap);

        // Make the POST request to the remote server that will handle the data.
        try
        {
            sendHTTPPost(finalDocumentData, new URL(System.getenv("POSTURL")));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void sendHTTPPost(String body, URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("POST request did not work.");
        }
    }

    // Clears all variables so they can be used for future use
    private void clearVariables()
    {
        this.monsterKills = new HashMap<String, Integer>();
        this.monsterLoot = new HashMap<String, String>();
    }

    public void startNPCCycle()
    {
        final Runnable databaseSender = new Runnable()
        {
            public void run()
            {
                System.out.println("Sending to database...");
                sendToDatabase();
                clearVariables();

                System.out.println("Sended to database, and variables cleared.");
            }
        };
        databaseSenderHandler =
                scheduler.scheduleAtFixedRate(databaseSender, this.npcTimeTreshold* 60L, this.npcTimeTreshold* 60L, SECONDS);
    }

    public void shutDown()
    {
        scheduler.execute(new Runnable() {
            public void run() {
                databaseSenderHandler.cancel(true);
            }
        });
    }


}
