package app.business;

@Informal
public class InformalGreeting extends Greeting {
    public String sayHelloToUser(final String user) {
        return "Hi " + user;
    }
}
