package service.mode;

public enum TreeEntryModeType {
    DIRECTORY(0b0100),
    REGULAR_FILE(0b1000),
    SYMBOLIC_LINK(0b1110),
    GITLINK(0b1110);


    private final int mask;
    private TreeEntryModeType(int value){
        this.mask = value;
    }

    public int shifted(){
        return mask << 12;
    }

    public boolean isPermissionless(){
        return this!=REGULAR_FILE ;
    }

    public static TreeEntryModeType match(int value){
        value>>=12;
        for(var mode:values()){
            var mask = mode.mask;
            if((value)==mask){
                return mode;
            }
        }

        throw new IllegalArgumentException("Unknown mode: "+value);
    }


}
