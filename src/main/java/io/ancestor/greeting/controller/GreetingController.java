package io.ancestor.greeting.controller;


import io.ancestor.greeting.response.GreetingResponse;
import io.ancestor.greeting.response.LocationResponse;
import io.ancestor.greeting.response.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class GreetingController {

    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

//    private static final String WEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY");

    @Value("${weather.api.key}")
    private String weatherApiKey;

    @GetMapping("/api/greeting")
    public GreetingResponse hello(@RequestParam(value = "visitor_name") String visitorName, @RequestParam(value = "ip", required = false) String clientIp, @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = (forwardedIp != null && !forwardedIp.isEmpty()) ? forwardedIp : "127.0.0.1"; // Default IP if not forwarded
        } else {
            logger.info("Using provided IP: {}", clientIp);
        }
        logger.info("Client IP: {}", clientIp);
        logger.info("Weather API Key: {}", weatherApiKey);

        if (weatherApiKey == null || weatherApiKey.isEmpty()) {
            logger.error("Weather API Key is not set!");
            return new GreetingResponse(clientIp, "Unknown", "API Key not set!");
        }

        String location = getLocation(clientIp);
        if (location.equals("Unknown") || location.isEmpty()) {
            logger.error("Failed to resolve location for IP: {}", clientIp);
            return new GreetingResponse(clientIp, "Unknown", "Failed to resolve location");
        }

        String temperature = getTemperature(location);
        if (temperature.equals("N/A")) {
            logger.error("Failed to get temperature for location: {}", location);
            return new GreetingResponse(clientIp, location, "Failed to get temperature");
        }

        return new GreetingResponse(clientIp, location, "Hello, " + visitorName + "!, the temperature is " + temperature + " in " + location);
    }


    private String getLocation(String clientIp) {
        // Handle localhost scenario with a default city
        if (clientIp.equals("127.0.0.1") || clientIp.equals("localhost")) {
            logger.info("Client IP is localhost: {}", clientIp);
            return "London"; // Use a default city for localhost
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://freegeoip.app/json/" + clientIp;
        logger.info("Fetching location data from URL: {}", url);

        try {
            ResponseEntity<LocationResponse> responseEntity = restTemplate.getForEntity(url, LocationResponse.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                LocationResponse response = responseEntity.getBody();

                if (response != null && response.getCity() != null) {
                    logger.info("Location response: {}", response);
                    return response.getCity();
                } else {
                    logger.warn("Location not found for IP: {}", clientIp);
                    return "Unknown";
                }
            } else {
                logger.error("Failed to fetch location data. Status code: {}", responseEntity.getStatusCode());
                return "Unknown";
            }
        } catch (RestClientException e) {
            logger.error("Exception while fetching location data: {}", e.getMessage());
            return "Unknown";
        }
    }

    private String getTemperature(String location) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + location + "&units=metric&appid=" + weatherApiKey;
        logger.info("Fetching weather data from URL: {}", url);

        try {
            ResponseEntity<WeatherResponse> responseEntity = restTemplate.getForEntity(url, WeatherResponse.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                WeatherResponse response = responseEntity.getBody();

                if (response != null && response.getMain() != null) {
                    logger.info("Weather response: {}", response);
                    return response.getMain().getTemp() + " degrees Celsius";
                } else {
                    logger.warn("Weather data not found for location: {}", location);
                    return "N/A";
                }
            } else {
                logger.error("Failed to fetch weather data. Status code: {}", responseEntity.getStatusCode());
                return "N/A";
            }
        } catch (RestClientException e) {
            logger.error("Exception while fetching weather data: {}", e.getMessage());
            return "N/A";
        }
    }
}