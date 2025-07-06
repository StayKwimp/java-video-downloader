package me.staykwimp.jvd;

public interface Visitable {
    public <R> R accept(BaseVisitor<R> visitor);
}
