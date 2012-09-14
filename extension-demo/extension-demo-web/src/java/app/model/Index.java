package app.model;

import app.business.Greeting;
import javax.enterprise.inject.Model;
import javax.inject.Inject;

@Model
public class Index {
    @Inject
    Greeting greeting;
    String userName;
    String message;

    public void sayHello() {
        message = greeting.sayHelloToUser(userName);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }
}
