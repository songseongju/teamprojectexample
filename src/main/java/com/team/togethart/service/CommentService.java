package com.team.togethart.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.team.togethart.dto.comment.CommentAddRequest;
import com.team.togethart.dto.comment.CommentViewResponse;
import com.team.togethart.repository.comment.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentViewResponse commentViewResponse;
    @Autowired
    private CommentAddRequest commentAddRequest;

    public void addComment(CommentAddRequest commentAddRequest) {

        if (commentAddRequest.getCommentContent() != null) {
            commentMapper.insertComment(commentAddRequest);
        }

    }

    public List<CommentViewResponse> getCommentList(Long artworkId) {
        return commentMapper.selectCommentList(artworkId);
    }


    @Service
    public static class OAuthService {

                //카카오 토큰 얻어오기
        public String getKakaoAccessToken (String code) {
            String access_Token = "";
            String refresh_Token = "";
            String reqURL = "https://kauth.kakao.com/oauth/token";

            try {
                URL url = new URL(reqURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //POST 요청을 위해 기본값이 false인 setDoOutput을 true로
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                //POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                StringBuilder sb = new StringBuilder();
                sb.append("grant_type=authorization_code");
                sb.append("&client_id=adbbf5ce440e188cfcb07e0628098935"); // TODO REST_API_KEY 입력
                sb.append("&redirect_uri=http://localhost:8080/oauth/kakao"); // TODO 인가코드 받은 redirect_uri 입력
                sb.append("&code=" + code);

                bw.write(sb.toString());
                bw.flush();

                int responseCode = conn.getResponseCode();
                System.out.println("responseCode : " + responseCode);

                //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                String result = "";

                while ((line = br.readLine()) != null) {
                    result += line;
                }
                System.out.println("response body : " + result);

                //Gson 라이브러리에 포함된 클래스로 JSON파싱

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(result);

                access_Token = element.getAsJsonObject().get("access_token").getAsString();
                refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

                System.out.println("access_token : " + access_Token);
                System.out.println("refresh_token : " + refresh_Token);

                br.close();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return access_Token;
        }

        public HashMap<String, Object> getUserInfo (String access_Token) {

            //    요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
            HashMap<String, Object> userInfo = new HashMap<>();
            String reqURL = "https://kapi.kakao.com/v2/user/me";
            try {
                URL url = new URL(reqURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                //    요청에 필요한 Header에 포함될 내용
                conn.setRequestProperty("Authorization", "Bearer " + access_Token);

                int responseCode = conn.getResponseCode();
                System.out.println("responseCode : " + responseCode);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = "";
                String result = "";

                while ((line = br.readLine()) != null) {
                    result += line;
                }
                System.out.println("response body : " + result);

                JsonParser parser = new JsonParser();
                JsonElement element = parser.parse(result);

                JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
                JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

                String nickname = properties.getAsJsonObject().get("nickname").getAsString();
                //String email = kakaoAccount.getAsJsonObject().get("email").getAsString();

                userInfo.put("nickname", nickname);
                //userInfo.put("email", email);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return userInfo;
        }



    /*
        public HashMap<String, Object> getUserInfo(String accessToken) {
            HashMap<String, Object> userInfo = new HashMap<String, Object>();
            String reqUrl = "https://kapi.kakao.com/v2/user/me";
            try {
                URL url = new URL(reqUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", " Bearer " + accessToken);
                int responseCode = conn.getResponseCode();
                System.out.println("responseCode =" + responseCode);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = "";
                String result = "";

                while((line = br.readLine()) != null) {
                    result += line;
                }
                System.out.println("response body ="+ result);

                JsonParser parser = new JsonParser();
                JsonElement element =  parser.parse(result);

                JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
                JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_Account").getAsJsonObject();

                String nickname = properties.getAsJsonObject().get("nickname").getAsString();
                String email = kakaoAccount.getAsJsonObject().get("email").getAsString();

                userInfo.put("nickname", nickname);
                userInfo.put("email", email);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return userInfo;
        }
    */
    public void kakaoLogout(String accessToken) {
        String reqURL = "http://kapi.kakao.com/v1/user/logout";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String result = "";
            String line = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    }
}