package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Developed by deepesh Samtani
 */
public class App {
    public static void main(String[] args) {
        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(App::run, 0, 30, TimeUnit.SECONDS);

    }

    private static void run() {
        System.out.println("Running: " + new java.util.Date());

        try(InputStream input = new FileInputStream("./src/main/resources/application.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            int count = 0;
            Date date;
            String strDate;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            for (int j = 0; j < 7; j++) {

                if(j==0){ //current date
                    date = new Date();
                    strDate = formatter.format(date);
                }
                else{ //Next 6 days
                    c.add(Calendar.DATE, 1);
                    Date currentDatePlusOne = c.getTime();
                    strDate = formatter.format(currentDatePlusOne);
                }
                System.out.println("Checking for date : "+strDate + ", Pin Code "+prop.getProperty("pin_code") + ", Age : "+ prop.get("age_limit"));

                URL url = new URL(
                        "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin?pincode="+prop.getProperty("pin_code")+"&date="
                                + strDate);// your url i.e fetch data from .

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }
                BufferedReader br = null;
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response;

                while ((response = br.readLine()) != null) {
                    JsonObject convertedObject = new Gson().fromJson(response, JsonObject.class);
                    JsonArray js = (JsonArray) convertedObject.get("sessions");

                    for (int i = 0; i < js.size(); i++) {

                        JsonObject product = (JsonObject) js.get(i);

                        JsonObject hm = product;
                        if (prop.getProperty("age_limit").equalsIgnoreCase(String.valueOf(hm.get("min_age_limit")))) {
                            if (i==0)
                                System.out.println("Total locaions Available : "+js.size());
                            if (Double.valueOf(String.valueOf(hm.get("available_capacity"))) > 0) {
                                count++;
                                System.out.print(hm.get("name") + ",");
                            }

                        }
                    }

                }


                System.out.println("\n\n");
            }
            if (count > 0) {
                for (int i = 0; i < 5; i++) {
                    Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(1000);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
