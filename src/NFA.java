import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class NFA implements Cloneable {
    private HashSet<Transition> transitions = new HashSet<Transition>();
    private HashSet<State> states = new HashSet<State>();
    private HashSet<State> accepting = new HashSet<State>();
    private State start;

    /* Constructors */
    public NFA() {
        // Blank constructor. Invalid NFA though!
    }

    public NFA(State start) {
        setStart(start);
    }

    /* Getters and setters */
    public State getStart() {
        return start;
    }

    public void setStart(State start) {
        this.start = start;
        addState(start);
    }

    public HashSet<Transition> getTransitions() {
        return transitions;
    }

    public HashSet<State> getStates() {
        return states;
    }

    public HashSet<State> getAcceptingStates() {
        return accepting;
    }

    /* Traversal */
    public Boolean isAccepted(String string) {
        return false;
    }

    public State step(State from, Character on) {
        return from;
    }

    // Which states can be reached on a certain character?
    public HashSet<State> statesReachableOn(State from, Character on) {
        HashSet<State> reachable = new HashSet<State>();
        Iterator<Transition> iter = from.getTransitions().iterator();
        while (iter.hasNext()) {
            Transition t = iter.next();

            // Do they want the epsilon transitions?
            boolean equals = false;
            if (on == null)
                equals = on == t.c;
            else
                equals = on.equals(t.c);

            // Add all matching transitions
            // Skip if already added to prevent loops
            if (equals  && !reachable.contains(t.to)) {
                // Add this state
                reachable.add(t.to);

                // Recurse and add all reachable from this state (epsilon transitions)
                reachable.addAll(statesReachableFrom(t.to));
            }
        }

        return reachable;
    }

    // Which states can be reached via empty string?
    public HashSet<State> statesReachableFrom(State from) {
        return statesReachableOn(from, null);
        //		HashSet<State> reachable = new HashSet<State>();
        //		Iterator<Transition> iter = from.getTransitions().iterator();
        //		while (iter.hasNext()) {
        //			Transition t = iter.next();
        //			
        //			// Add all empty transitions
        //			// Skip if already added to prevent loops
        //			if (t.isEmptyTransition() && !reachable.contains(t.to)) {
        //				// Add this state
        //				reachable.add(t.to);
        //				
        //				// Recurse and add all reachable from this state
        //				reachable.addAll(statesReachableFrom(t.to));
        //			}
        //		}
        //		
        //		return reachable;
    }

    /* Manipulation */
    public State addState(State state) {
        this.states.add(state);
        state.setNFA(this);

        if (state.getAccepts())
            accepting.add(state);

        return state;
    }

    public Transition addTransition(State from, Character on, State to) {
        if (!equals(from.getNFA()) && from.getNFA() != null) {
            transitions.addAll(from.getNFA().getTransitions());
            Iterator<State> iters = from.getNFA().getStates().iterator();
            while(iters.hasNext()) {
                State state = iters.next();
                addState(state);
            }

            states.addAll(from.getNFA().getStates());
        } else {
            from.setNFA(this);
            states.add(from);
        }

        if (!equals(to.getNFA()) && to.getNFA() != null) {
            transitions.addAll(to.getNFA().getTransitions());
            Iterator<State> iters = to.getNFA().getStates().iterator();
            while(iters.hasNext()) {
                State state = iters.next();
                addState(state);
            }

            states.addAll(to.getNFA().getStates());
        } else {
            to.setNFA(this);
            states.add(to);
        }

        Transition transition = new Transition(from, on, to);
        this.transitions.add(transition);
        from.addTransition(on, to);

        return transition;
    }

    public boolean getAccepts(State state) {
        return state.getAccepts();
    }

    public void setAccepts(State state, boolean accepts) {
        if (accepts)
            accepting.add(state);
        else
            accepting.remove(state);

        // Prevent loops
        if (state.getAccepts() != accepts)
            state.setAccepts(accepts);
    }
    
    public NFA recreateNFA(){
        HashSet<State> states = (HashSet<State>) this.states.clone();
        HashSet<Transition> transitions = this.transitions;
        State start = this.start;
        NFA nfa = new NFA();
        Iterator<State> statIter = states.iterator();
        
        Iterator<Transition> tranIter = transitions.iterator();
        while(tranIter.hasNext()){
            Transition t = tranIter.next();
            State to = (State) t.to.clone();
            State from = (State) t.from.clone();
            nfa.transitions.add(new Transition(from, t.c, to));
        }
        
        while(statIter.hasNext()){
            State s = statIter.next();
            State newS = s.rename();
            newS.setNFA(nfa);
            
            if(s.getCount() == start.getCount()){
                nfa.setStart(newS);
            }
            if(s.getAccepts()){
                nfa.accepting.add(newS);
            }
            
            nfa.states.add(newS);

            Iterator<Transition> newTranIter = nfa.transitions.iterator();
            while(newTranIter.hasNext()){
                Transition newT;
                Transition t = newTranIter.next();
                State to = (State)t.to;
                State from = (State) t.from;
                Character c = t.c;
                if(to.getCount() == s.getCount()) {
                    t.to = newS;
                }
                if(from.getCount() == s.getCount()) {
                    t.from = newS;
                }
            }
        }
//        this.states.remove(start);
//        states.add(startClone);
        
        return nfa;
    }

    public static NFA union(NFA nfa1, NFA nfa2){
        if(nfa1 == null){
            return (NFA) nfa2.clone();
        }
        if(nfa2 == null){
            return (NFA) nfa1.clone();
        }
        
//        HashSet<State> states1 = ((NFA) nfa1.clone()).getStates();
//        HashSet<State> states2 = ((NFA) nfa2.clone()).getStates();
//        HashSet<State> newStates1 = new HashSet<State>();
//        Iterator<State> iter1 = states1.iterator();
//        boolean foundMatch = false;
//        while(iter1.hasNext() && !foundMatch){
//            State s1 = iter1.next();
//            Iterator<State> iter2 = states2.iterator();
//            while(iter2.hasNext()){
//                State s2 = iter2.next();
//                if(s1.equals(s2)){
//                    nfa1.recreateNFA();
//                    foundMatch = true;
//                    break;
//                }
//            }
//        }
//        if(foundMatch){
//            states1 = newStates1;
//        }
        
        State newStart = new State();
        newStart.setLabel("Start");
        NFA newNfa = new NFA(newStart);
        newNfa.setAccepts(newStart, false);
        newNfa.addEpsilonTransition(newStart, nfa1.getStart());
        newNfa.addEpsilonTransition(newStart, nfa2.getStart());
        return newNfa;
    }

    public static NFA concat(NFA nfa1, NFA nfa2){
        if(nfa1 == null){
            return nfa2;
        }
        if(nfa2 == null){
            return nfa1;
        }

//        nfa1 = (NFA) nfa1.clone();
//        nfa2 = (NFA) nfa2.clone();
//        HashSet<State> states1 = nfa1.getStates();
//        HashSet<State> states2 = nfa2.getStates();
//        Iterator<State> iter1 = states1.iterator();
//        boolean foundMatch = false;
//        while(iter1.hasNext() && !foundMatch){
//            State s1 = iter1.next();
//            Iterator<State> iter2 = states2.iterator();
//            while(iter2.hasNext()){
//                State s2 = iter2.next();
//                if(s1.equals(s2)){
//                    nfa1.recreateNFA();
//                    foundMatch = true;
//                    break;
//                }
//            }
//        }
        
        State startState = nfa2.getStart();
        HashSet<State> acceptingStates = nfa1.getAcceptingStates();
        Iterator<State> iter = acceptingStates.iterator();
        ArrayList<Transition> transitionsToAdd = new ArrayList<Transition>();
        ArrayList<State> statesToChange = new ArrayList<State>();
        while(iter.hasNext()){
            State s = iter.next();
            statesToChange.add(s);
            Transition t = new Transition(s, Transition.EPSILON, startState);
            transitionsToAdd.add(t);
        }
        for(State s: statesToChange) {
            nfa1.setAccepts(s, false);
        }
        for(Transition t: transitionsToAdd) {
            nfa1.addTransition(t.from, t.c, t.to);
        }

        return nfa1;
    }

    public static NFA star(NFA nfa1){
        State startState = nfa1.getStart();
        HashSet<State> acceptingStates = nfa1.getAcceptingStates();
        Iterator<State> iter = acceptingStates.iterator();
        while(iter.hasNext()){
            State s = iter.next();
            nfa1.addEpsilonTransition(s, startState);
        }
        State newStart = new State();
        newStart.setLabel("Start");
        NFA newNfa = new NFA(newStart);
        newNfa.setAccepts(newStart, true);
        newNfa.addEpsilonTransition(newStart, startState);

        return newNfa;
    }
    public static NFA plus(NFA nfa1){
        State startState = nfa1.getStart();
        HashSet<State> acceptingStates = nfa1.getAcceptingStates();
        Iterator<State> iter = acceptingStates.iterator();
        while(iter.hasNext()){
            State s = iter.next();
            nfa1.addEpsilonTransition(s, startState);
        }

        return nfa1;
    }

    // Add empty string transition
    public Transition addEpsilonTransition(State from, State to) {
        return addTransition(from, Transition.EPSILON, to);
    }

    /* Export */
    public State[][] toTable() {
        // Should return some kind of transition table
        return toDFA().toTable();
    }

    public DFA toDFA() {
        return new DFA(this);
    }

    public String toString() {
        String s = "";
        s += "Start: "+start+"\n";
        s += ""+states.size()+" states: ";

        s += states.toString();
        //		s += "[";
        //		int i = 0;
        //		for (State state : states) {
        //			if (start.equals(state)) s += "=>";
        //			s += state.toString();
        //			if (i != states.size() - 1) s += ", ";
        //			i++;
        //		}
        s += "\n";

        s += ""+transitions.size()+" transitions: ";
        s += transitions.toString();

        return s;
    }

    public void setKlass(Terminals klass){
        for(State state : states){
            state.klass = klass;
        }
    }
    
    @Override
    public Object clone(){
        try {
            NFA nfa = (NFA) super.clone();
            nfa.accepting = (HashSet<State>) accepting.clone();
            nfa.start = (State) start.clone();
            nfa.states = (HashSet<State>) states.clone();
            nfa.transitions = (HashSet<Transition>) transitions.clone();
            return nfa;
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
