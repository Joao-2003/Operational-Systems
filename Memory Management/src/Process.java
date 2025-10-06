class Process {
    private final String id;
    private final int size;

    public Process(String id, int size) {
        this.id = id;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return id + " (tamanho " + size + ")";
    }
}