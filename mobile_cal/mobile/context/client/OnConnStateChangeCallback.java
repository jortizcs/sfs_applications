public abstract class OnConnStateChangeCallback{
    public enum State {
        ACCESSIBLE, NOT_ACCESSIBLE
    }

    public OnConnStateChangeCallback(){}

    public void stateChanged(State state){
    }
}
