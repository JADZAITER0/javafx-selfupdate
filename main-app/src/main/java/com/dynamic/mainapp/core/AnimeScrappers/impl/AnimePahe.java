package com.example.selfupdate.testjavafxmvci.core.AnimeScrappers.impl;

import com.example.selfupdate.testjavafxmvci.models.Anime;
import com.example.selfupdate.testjavafxmvci.models.AnimeProvider;
import com.example.selfupdate.testjavafxmvci.models.Episode;
import com.example.selfupdate.testjavafxmvci.core.AnimeScrappers.AnimeScrapper;
import com.example.selfupdate.testjavafxmvci.utils.DecryptPostForm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.debatty.java.stringsimilarity.JaroWinkler;
import javafx.concurrent.Task;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimePahe implements AnimeScrapper {
    //Just for this Website we will cache the animes names and Ids
    private Map<String,String> animeIdsMap;
    private Map<String, Set<String>> bigramIndex;
    private List<String> animeNames;
    private JaroWinkler jaroWinkler;


    private final String BASE_URL = "https://animepahe.si";


    public AnimePahe(){
        Task<Void> init = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                init();
                return null;
            }
        };

        new Thread(init).start();
    }


    @Override
    public List<Anime> getNewReleases(int pageNumber) {
        String target = BASE_URL + "/api?m=airing&page=" + pageNumber;
        Document response = null;
        try {
            response = Jsoup.connect(target)
                    .cookie("__ddg2_", "")
                    .cookie("__ddg1_", "")
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response.body().text());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonNode dataNode = rootNode.path("data");
        if (dataNode.isMissingNode()){
            return Collections.emptyList();
        }
        List<Anime> animeList = new ArrayList<>(10);
       for (JsonNode animeNode : dataNode) {
           String title = animeNode.get("anime_title").asText();
           String id = animeNode.get("anime_session").asText();

           if (!animeIdsMap.containsKey(title)) {
               animeIdsMap.put(title, id);
               animeNames.add(title);
           }

           animeList.add(new Anime(id,title));
       }


        return animeList;
    }

    @Override
    public List<Anime> searchAnimeByName(String animeName, int pageNumber) {
        //headers.keySet().forEach(key -> {if key.contains(animeName)})
        Set<String> candidates = getCandidates(animeName);
        String normalizedQuery = animeName.toLowerCase();

        int pageSize = 15; // number of results per page
        int skip = (pageNumber - 1) * pageSize; // how many results to skip

        return candidates.stream()
                .filter(name -> jaroWinkler.similarity(normalizedQuery, name.toLowerCase()) > 0.7)
                .sorted(Comparator.comparingDouble(name -> -jaroWinkler.similarity(normalizedQuery, name.toLowerCase())))
                .skip(skip)
                .limit(pageSize)
                .collect(LinkedList::new, (list, name) -> list.add(new Anime(animeIdsMap.get(name),name)), LinkedList::addAll);

    }


    @Override
    public Anime getAnimeDetails(Anime anime){
        String target = BASE_URL + "/anime/" + anime.getProviderId(AnimeProvider.ANIME_PAHE);
        Document response = null;
        try {
            response = Jsoup.connect(target)
                    .cookie("__ddg2_", "")
                    .cookie("__ddg1_", "")
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }




        Element info = response.getElementsByClass("anime-info").first();
        String type = info.select("p:has(strong:contains(Type)) a").text();




        String status = info.select("p:has(strong:contains(Status)) a").text();
        String episodes = info.select("p:has(strong:contains(Episode))").text().replace("Episodes*:", "").trim();

        int episodeCount = episodes.contains("[0-9]+")? Integer.parseInt(episodes) : 0;
        int currentEpisodes = 0;
        if (status.contains("Finis")){
            currentEpisodes = episodeCount;
        }

        if (episodeCount == 0){
            String episodeTarget = BASE_URL + "/api?m=release&id=" + anime.getProviderId(AnimeProvider.ANIME_PAHE);
            Document episodeResponse = null;

            try {
                episodeResponse = Jsoup.connect(episodeTarget)
                        .cookie("__ddg2_", "")
                        .cookie("__ddg1_", "")
                        .ignoreContentType(true)
                        .get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;

            try {
                rootNode = objectMapper.readTree(episodeResponse.body().text());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            currentEpisodes = rootNode.get("total").asInt();
        }



        String duration = info.select("p:has(strong:contains(Duration))").text().replace("Duration:", "").trim();
        String aired = info.select("p:has(strong:contains(Aired))").text().replace("Aired:", "").trim();
        String season = info.select("p:has(strong:contains(Season)) a").text();
        String studio = info.select("p:has(strong:contains(Studio))").text().replace("Studio:", "").trim();
        String demographic = info.select("p:has(strong:contains(Demographic)) a").text();
        System.out.println("Studio:" + studio);

        Element animePoster = response.getElementsByClass("anime-poster").first();
        String imageUrl = animePoster.selectFirst("a").attr("href");

        String description = response.getElementsByClass("anime-synopsis").text();

        Elements genres = info.select(".anime-genre ul li a");
        String[] genreArray = new String[genres.size()];
        for (Element genre : genres) {
            genreArray[genres.indexOf(genre)] = genre.text();
        }

        Element malLink = response.selectFirst("p.external-links a[href*='myanimelist.net/anime/']");
        String linkMAL = malLink.attr("href");
        String malId = linkMAL.substring(linkMAL.lastIndexOf("/")+1);

        anime.setDescription(description)
                .setAnimeType(type)
                .setGenre(genreArray)
                .setReleaseDate(aired)
                .setSeason(season)
                .setStatus(status)
                .setMALId(malId)
                .setImageURL(imageUrl)
                .setCurrentEpisode(currentEpisodes)
                .setEpisodeCount(episodeCount);

        return anime;
    }

    @Override
    public List<Episode> getEpisodesList(Anime anime) {
        String animeId = anime.getProviderId(AnimeProvider.ANIME_PAHE);
        String target = BASE_URL + "/api?m=release&id=" + animeId;// +"&sort=episode_asc";
        Document firstResponse = null;
        try {
            firstResponse = Jsoup.connect(target)
                    .cookie("__ddg2_", "")
                    .cookie("__ddg1_", "")
                    .ignoreContentType(true)
                    .get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(firstResponse.body().text());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonNode dataNode = rootNode.path("data");
        if (dataNode.isMissingNode()){
            return Collections.emptyList();
        }

        String lastPage = rootNode.path("last_page").asText();
        System.out.println(animeId);
        int total = rootNode.path("total").asInt();
        int perPage = rootNode.path("per_page").asInt();
        int index = (total < perPage)? total : perPage;

        String session = dataNode.get(0).get("session").asText();

        String lastAiredEpisodeDate = dataNode.get(0).get("created_at").asText();
        anime.refreshUpdatedAt(lastAiredEpisodeDate);

        System.out.println("Last aired episode date: " + lastAiredEpisodeDate);

        int i = 0;
        List<Episode> episodes = new ArrayList<>(total);
        if (lastPage.equals("1")){;
            for (JsonNode episodeNode : dataNode) {
                String episodeId = episodeNode.get("session").asText();


                Episode episode = new Episode(animeId,total - i++, episodeId, AnimeProvider.ANIME_PAHE);
                episodes.add(episode);
            }
            return episodes.reversed();
        }

        //If more than 1 page, we need to make another request to get all episodes
        Document secondResponse = null;
        try {
            secondResponse = Jsoup.connect(BASE_URL + "/play/" + animeId + "/" + session)
                    .cookie("__ddg2_", "")
                    .cookie("__ddg1_", "")
                    .ignoreContentType(true)
                    .get();

            Element episodesMenu = secondResponse.getElementsByClass("episode-menu").first();
            Elements episodeLinks = secondResponse.select(".dropdown-menu #scrollArea a.dropdown-item");
            for (Element episode : episodeLinks) {
                String href = episode.attr("href");
                System.out.println("Href: " + href.replaceAll("/play/.*/" , ""));
                Episode ep = new Episode(animeId, ++i, href.replaceAll("/play/.*/" , ""), AnimeProvider.ANIME_PAHE);
                episodes.addLast(ep);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return episodes;
    }

    @Override
    public Map.Entry<String,String> getEpisodeDownloadLinks(Episode episode, String quality, String subOrDub) {
        System.out.println(episode.getEpisodeId());
        System.out.println(episode.getAnimeId());
        String target = BASE_URL + "/play/" + episode.getAnimeId() + "/" +episode.getEpisodeId();
        System.out.println(target);
        Document response = null;
        try {
            response = Jsoup.connect(target)
                    .cookie("__ddg2_", "")
                    .cookie("__ddg1_", "")
                    .ignoreContentType(true)
                    .timeout(5000)
                    .get();
        } catch (IOException e) {
            System.out.println("Error fetching episode download links: " + e.getMessage());
            throw new RuntimeException(e);
        }
        Elements links = response.select("#pickDownload a.dropdown-item");
        if (links.isEmpty()) return null;

        int requestedQuality = extractQualityValue(quality);
        Element bestMatch = null;
        int closestDiff = Integer.MAX_VALUE;

        for (Element link : links) {
            String text = link.text().toLowerCase();
            boolean isDub = text.contains("eng");

            // Skip links that don't match the sub/dub preference
            if ("dub".equalsIgnoreCase(subOrDub) && !isDub) continue;
            if ("sub".equalsIgnoreCase(subOrDub) && isDub) continue;

            int q = extractQualityValue(text);
            int diff = Math.abs(q - requestedQuality);

            if (diff < closestDiff) {
                closestDiff = diff;
                bestMatch = link;
            }
        }

        if (bestMatch == null) {
            for (Element link : links) {
                int q = extractQualityValue(link.text());
                int diff = Math.abs(q - requestedQuality);
                if (diff < closestDiff) {
                    closestDiff = diff;
                    bestMatch = link;
                }
            }
        }

        // Build and return the result
        if (bestMatch != null) {
            String link = bestMatch.attr("href");
            Document kiwikDocument = null;
            try {
                kiwikDocument = Jsoup.connect(link)
                        .referrer(target)
                        .header("Connection", "keep-alive")
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Pattern pattern = Pattern.compile("https?://kwik.cx/f/([^\"']+)");
            Matcher matcher = pattern.matcher(kiwikDocument.html());

            if (matcher.find()) {
                String kwikUrl = matcher.group();
                Connection.Response kwikResponse = null;
                Document kwikDocument = null;
                try {
                    kwikResponse = Jsoup.connect(kwikUrl)
                            .header("Connection", "keep-alive")
                            .execute();
                    kwikDocument = kwikResponse.parse();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String regex = "\\(\"(\\w+)\",\\d+,\"(\\w+)\",(\\d+),(\\d+),(\\d+)\\)";
                Pattern pattern2 = Pattern.compile(regex);

                // Match the input string against the pattern
                Matcher matcher2 = pattern2.matcher(kwikDocument.body().html());

                while (matcher2.find()) {
                    // Extract and print all groups
                    String fullKey = matcher2.group(1);
                    String key = matcher2.group(2);
                    int v1 = Integer.parseInt(matcher2.group(3));
                    int v2 = Integer.parseInt(matcher2.group(4));

                    Document docs = Jsoup.parse(DecryptPostForm.decryptPostForm(fullKey, key, v1, v2));
                    //System.out.println(docs.body());
                    String downloadUrl = docs.select("form").attr("action");
                    //System.out.println(downloadUrl);
                    String token = docs.select("input[name='_token']").attr("value");
                    Document postDoc = null;

                    try {
                        postDoc = Jsoup.connect(downloadUrl)
                                .header("Referer", kwikUrl)
                                .cookies(kwikResponse.cookies())
                                .data("_token", token)
                                .followRedirects(false)
                                .ignoreContentType(true)
                                .post();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(postDoc.body());
                    return Map.entry(postDoc.select("a").getLast().attr("href"), bestMatch.text());

                }
            }


            //return Map.entry(bestMatch.attr("href"), bestMatch.text());
        }
        return null;


    }

    private static Map<String, Set<String>> buildBigramIndex(List<String> animeNames) {
        Map<String, Set<String>> bigramIndex = new HashMap<>();
        for (String name : animeNames) {
            String normalized = name.toLowerCase();
            for (int i = 0; i < normalized.length() - 1; i++) {
                String bigram = normalized.substring(i, i + 2);
                bigramIndex.computeIfAbsent(bigram, k -> new HashSet<>()).add(name);
            }
        }
        return bigramIndex;
    }


    private Set<String> getCandidates(String query) {
        String normalized = query.toLowerCase();
        Set<String> candidates = new HashSet<>();

        for (int i = 0; i < normalized.length() - 1; i++) {
            String bigram = normalized.substring(i, i + 2);
            Set<String> names = bigramIndex.get(bigram);
            if (names != null) {
                candidates.addAll(names);
            }
        }

        if (candidates.isEmpty() || normalized.length() <= 2) {
            candidates.addAll(animeNames);
        }

        return candidates;
    }

    private int extractQualityValue(String text) {
        try {
            Matcher m = java.util.regex.Pattern.compile("(\\d{3,4})p").matcher(text);
            if (m.find()) return Integer.parseInt(m.group(1));
        } catch (Exception ignored) {}
        return 0;
    }

    private void init() {

        animeIdsMap = new HashMap<>();
        animeNames = new ArrayList<>();
        jaroWinkler = new JaroWinkler();
        bigramIndex = buildBigramIndex(animeNames);

        //Cache anime names and Ids from AnimePahe
        Connection connection = Jsoup.connect(BASE_URL+"/anime")
                .cookie("__ddg2_", "")
                .cookie("__ddg1_", "")
                .ignoreContentType(true)
                .method(Connection.Method.GET);
        Connection.Response response = null;

        try {
            response = connection.execute();

        } catch (Exception _) {};

        if (response != null && response.statusCode() == HttpsURLConnection.HTTP_OK) {
            Elements animeLinks = null;
            try {
                animeLinks = response.parse().select(".tab-content a[href]");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Iterate and extract href + title
            for (Element link : animeLinks) {
                String href = link.attr("href");
                String title = link.attr("title");

                // In case title is empty, use the text inside the <a> tag
                if (title.isEmpty()) {
                    title = link.text();
                }

                // Add the title to animeNames list (FIXED)
                if (!title.isEmpty()) {
                    animeNames.add(title);
                    animeIdsMap.put(title, href.replace("/anime/", ""));
                }
            }

        }
    }
}
