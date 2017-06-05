package com.tharindu.oauth.facebookapp.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class OAuthCallbackListener extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
            IOException {
// Detect the presence of an authorization code
        String authorizationCode = request.getParameter("code");
        if (authorizationCode != null && authorizationCode.length() > 0) {
            final String TOKEN_ENDPOINT =
                    "https://graph.facebook.com/oauth/access_token";
            final String GRANT_TYPE = "authorization_code";
            final String REDIRECT_URI = "http://localhost:8080/facebookapp/callback";
            final String CLIENT_ID = "1068254553306298";
            final String CLIENT_SECRET = "09b294ff18182985edddd004963a39e4";
            // Generate POST request
            HttpPost httpPost = new HttpPost(TOKEN_ENDPOINT +
                    "?grant_type=" + URLEncoder.encode(GRANT_TYPE,
                    StandardCharsets.UTF_8.name()) +
                    "&code=" + URLEncoder.encode(authorizationCode,
                    StandardCharsets.UTF_8.name()) +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI,
                    StandardCharsets.UTF_8.name()) +
                    "&client_id=" + URLEncoder.encode(CLIENT_ID,
                    StandardCharsets.UTF_8.name()));
// Add "Authorization" header with encoded client credentials
            String clientCredentials = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedClientCredentials =
                    new String(Base64.encodeBase64
                            (clientCredentials.getBytes()));
            httpPost.setHeader("Authorization", "Basic " +
                    encodedClientCredentials);
// Make the access token request
            CloseableHttpClient httpClient =
                    HttpClients.createDefault();
            HttpResponse httpResponse = httpClient.execute(httpPost);
// Handle access token response
            Reader reader = new InputStreamReader
                    (httpResponse.getEntity().getContent());
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
// Isolate access token
            String accessToken = null;
            String[] responseProperties = line.split("&");
            for (String responseProperty : responseProperties) {
                System.out.println(responseProperty);
                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(responseProperty);
                    JSONObject jsonobj = (JSONObject) obj;
                    accessToken = jsonobj.get("access_token").toString();
                    System.out.println("Access token: " + accessToken);
                } catch (ParseException e) {
                    System.out.println("Error while parsing the response from facebook : " + e.getMessage());
                }

            }
            // Request profile and feed data with access token
// Request feed data with access token
            String requestUrl =
                    "https://graph.facebook.com/v2.8/me/feed?limit=25";

            httpPost = new HttpPost(requestUrl);
            httpPost.addHeader("Authorization", "Bearer " + accessToken);
            List<NameValuePair> urlParameters = new
                    ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("method", "get"));
            httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
            httpResponse = httpClient.execute(httpPost);
// Extract feed data from response
            bufferedReader = new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity().getContent()));
            String feedJson = bufferedReader.readLine();
            System.out.println("Feed data: " + feedJson);


            httpClient.close();
        } else {
// Handle failure
        }
    }
}

