package euphoria.psycho.download;public abstract class Request<T> implements Comparable<Request<T>> {    public void cancel() {    }    public void addMarker(String marker) {    }    public void setSequence(int sequence) {    }    public void setRequestQueue(RequestQueue queue) {    }    public boolean shouldCache() {        return false;    }    public Object getTag() {        return null;    }}
