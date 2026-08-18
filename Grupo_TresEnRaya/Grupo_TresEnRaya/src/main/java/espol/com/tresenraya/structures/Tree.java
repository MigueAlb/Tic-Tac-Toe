package espol.com.tresenraya.structures;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Beto
 */
import java.util.LinkedList;

public class Tree<E> {

    private NodeTree<E> root;

    public Tree() {
        this.root = null;
    }

    public Tree(E content) {
        this.root = new NodeTree<>(content);
    }

    public Tree(NodeTree<E> root) {
        this.root = root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public NodeTree<E> getRoot() {
        return root;
    }

    public void setRoot(NodeTree<E> root) {
        this.root = root;
    }

    public Tree<E> addChild(E content) {
        if (this.isEmpty()) {
            throw new IllegalStateException("El árbol debe tener una raíz");
        }

        Tree<E> child = new Tree<>(content);
        root.addChild(child);

        return child;
    }

    public void recorrerPreorden() {
        if (this.isEmpty()) {
            return;
        }

        System.out.println(root.getContent());

        for (Tree<E> child : root.getChildren()) {
            child.recorrerPreorden();
        }
    }

    public LinkedList<E> breadthFirst() {
        LinkedList<E> contents = new LinkedList<>();

        if (this.isEmpty()) {
            return contents;
        }

        LinkedList<Tree<E>> queue = new LinkedList<>();
        queue.offer(this);

        while (!queue.isEmpty()) {
            Tree<E> current = queue.poll();

            contents.add(
                    current.getRoot().getContent()
            );

            queue.addAll(
                    current.getRoot().getChildren()
            );
        }

        return contents;
    }

    public int size() {
        if (this.isEmpty()) {
            return 0;
        }

        int total = 1;

        for (Tree<E> child : root.getChildren()) {
            total += child.size();
        }

        return total;
    }
}
