package main;

public class Node {
    int value;
    double freq;
    Node left;
    Node right;
    int height;


    public Node(int value, double freq, Node left, Node right) {
        this.value = value;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public int getHeight(Node node) {
        if (node == null) {
            return 0;
        } else {
            return 1 +
                    Math.max(getHeight(node.left),
                            getHeight(node.right));
        }
    }


    @Override

    public String toString() {

        return "Value --> " + value + " Freq --> " + freq + " Height --> " + height;

    }
}

