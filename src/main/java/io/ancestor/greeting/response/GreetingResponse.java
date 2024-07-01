package io.ancestor.greeting.response;


public class GreetingResponse {
    private String clientIp;
    private String location;
    private String greeting;

    public GreetingResponse(String clientIp, String location, String greeting) {
        this.clientIp = clientIp;
        this.location = location;
        this.greeting = greeting;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
