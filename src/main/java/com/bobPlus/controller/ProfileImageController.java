package com.bobPlus.controller;

import com.bobPlus.service.ProfileImageService;
import com.bobPlus.dto.SessionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class ProfileImageController {

    @Value("${profile.image.dir}")
    private String imgDir;
    ProfileImageService imageService;

    @GetMapping("/images/{profile}")
    public Resource downloadImage(@PathVariable String profile) throws MalformedURLException {
        return new UrlResource("file:" + imgDir + profile);
    }

    @PostMapping("/mypage/picture")
    public ResponseEntity<HashMap<String, Object>> uploadProfile(@RequestParam MultipartFile file, HttpServletRequest req) {

        ResponseEntity<HashMap<String, Object>> entity;
        HashMap<String, Object> resultMap = new HashMap<>();
        SessionDto sessionDto = (SessionDto) req.getSession().getAttribute("session");

        try {
            long result = imageService.uploadProfile(file, sessionDto.getUserId());
            resultMap.put("interCode", result);
            entity = new ResponseEntity<>(resultMap, HttpStatus.OK);
        } catch (IOException e) {
            long result = -15L;
            resultMap.put("interCode", result);
            entity = new ResponseEntity<>(resultMap, HttpStatus.FORBIDDEN);
        }
        return entity;
    }
}