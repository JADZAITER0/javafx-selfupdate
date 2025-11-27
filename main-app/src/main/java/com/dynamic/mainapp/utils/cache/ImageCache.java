package com.example.selfupdate.testjavafxmvci.utils.cache;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();


    public static Image get(String url) {
        //leaves only the poster ID
        String sanitizedUrl = url.replaceAll(".*posters.|.jpg","");



        //first check app cache
        if  (cache.containsKey(sanitizedUrl)) {
            return cache.get(sanitizedUrl);
        }

        //second check disk & put in cache
        File cachedImage = new File("cache/"+sanitizedUrl+".png");
        if (cachedImage.exists()) {
            Image fromDisk = new Image(cachedImage.toURI().toString(),true);
            cache.computeIfAbsent(sanitizedUrl, u -> fromDisk);
            return fromDisk;
        }

        //third load it from internet then put in cache and disk
        Image img = new Image(url,true);
        System.out.println(sanitizedUrl);
        img.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0){
                saveImageToDisk(sanitizedUrl, img);
                cache.put(sanitizedUrl, img);
            }
        });
        return img;

    }

    private static Image loadFromDisk(String path){
        Task<Image> task = new  Task<>() {
            @Override
            public Image call() throws Exception {
                return new Image(path);
            }
        };
        task.setOnSucceeded(event -> {
            Image img = task.getValue();
            cache.computeIfAbsent(path, u -> img);
        });
        new Thread(task).start();
        return task.getValue();

    }

    private static void saveImageToDisk(String url, Image img) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    File cache = new File("cache");
                    if  (!cache.exists()) {
                        cache.mkdir();
                    }
                    File output = new File(cache,url+".png");
                    WritableImage writable = new WritableImage(img.getPixelReader(),(int)img.getWidth(),(int)img.getHeight());
                    ImageIO.write(SwingFXUtils.fromFXImage(writable, null),"png",output);

                }catch (Exception _){
                }
                return null;
            }

        };

        new Thread(task).start();

    }

    public static void clear() {
        cache.clear();
    }
}