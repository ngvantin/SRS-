package com.example.hcm25_cpl_ks_java_01_lms.material;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;

public class YoutubeTimeEstimator {
    private static final String API_KEY = "AIzaSyCeQUfKucVxtH-l8JGQYZo928u2qr9mYjI";
    private static final YouTube youtube = new YouTube.Builder(
            new NetHttpTransport(),
            new JacksonFactory(),
            null)
            .setApplicationName("YourAppName")
            .build();

    public static long getVideoDurationInMinutes(String videoId) throws IOException {
        YouTube.Videos.List request = youtube.videos().list("contentDetails");
        request.setId(videoId);
        request.setKey(API_KEY);

        VideoListResponse response = request.execute();
        if (response.getItems() != null && !response.getItems().isEmpty()) {
            Video video = response.getItems().get(0);
            String duration = video.getContentDetails().getDuration();
            return parseIso8601Duration(duration); // Chuyển đổi định dạng ISO 8601 (PT1M30S) thành phút
        }
        throw new IOException("Video not found or API error");
    }

    public static long parseIso8601Duration(String duration) {
        try {
            // Kiểm tra nếu duration rỗng hoặc null
            if (duration == null || duration.isEmpty()) {
                return 0;
            }

            // Xóa ký tự không hợp lệ và chỉ lấy số
            String numericDuration = duration.replaceAll("[^0-9]", " "); // Loại bỏ ký tự chữ
            String[] parts = numericDuration.trim().split("\\s+"); // Tách các số thành mảng

            long hours = 0, minutes = 0, seconds = 0;

            // Kiểm tra và gán giá trị theo thứ tự
            if (parts.length == 3) { // 1H2M3S
                hours = Long.parseLong(parts[0]);
                minutes = Long.parseLong(parts[1]);
                seconds = Long.parseLong(parts[2]);
            } else if (parts.length == 2) { // 2M3S hoặc 1H2M
                if (duration.contains("H")) {
                    hours = Long.parseLong(parts[0]);
                    minutes = Long.parseLong(parts[1]);
                } else {
                    minutes = Long.parseLong(parts[0]);
                    seconds = Long.parseLong(parts[1]);
                }
            } else if (parts.length == 1) { // 1H hoặc 3M hoặc 45S
                if (duration.contains("H")) {
                    hours = Long.parseLong(parts[0]);
                } else if (duration.contains("M")) {
                    minutes = Long.parseLong(parts[0]);
                } else {
                    seconds = Long.parseLong(parts[0]);
                }
            }

            // Chuyển đổi tất cả thành phút
            return hours * 60 + minutes + (seconds / 60);
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing YouTube duration: " + duration);
            e.printStackTrace();
            return 0;
        }
    }


    public static void main(String[] args) throws IOException {
        String videoId = "dQw4w9WgXcQ"; // Ví dụ: video "Never Gonna Give You Up"
        long minutes = getVideoDurationInMinutes(videoId);
        System.out.printf("Estimated video duration: %d minutes%n", minutes);
    }
}