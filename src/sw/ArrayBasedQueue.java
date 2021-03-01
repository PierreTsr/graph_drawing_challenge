package sw;
import java.util.Arrays;

/**
 * @author Luca Castelli Aleardi (Ecole Polytechnique, 2019)
 * 
 * An array-based implementation of a queue
 */
public class ArrayBasedQueue<X> {
    private int front;
    private int rear;
    private final int maxSize;
    X[] A;

	/**
	 * Initialize a queue (with a given size)
	 */
    public ArrayBasedQueue(int size) {
        maxSize=size;
        A = (X[]) new Object[size];
        front = -1;
        rear = -1;
    }
    
	/**
	 * Add a value at the end of the queue.
	 */
    public void add(X value) {
        if ((rear+1)%maxSize==front) {
            throw new Error("Queue is full");

        } else if (isEmpty()) {
            front++;
            rear++;
            A[rear] = value;

        } else {
            rear=(rear+1)%maxSize;
            A[rear] = value;

        }
    }

	/**
	 * Removes the value at the top of this queue and returns that value as the value of this function.
	 */
    public X poll() {
        X value = null;
        if (isEmpty()) {
            throw new Error("Queue is empty, cant dequeue");
        } else if (front == rear) {
            value = A[front];
            front = -1;
            rear = -1;

        } else {
            value = A[front];
            front=(front+1)%maxSize;

        }
        return value;
    }

	/**
	 * Check whether the stack is empty
	 */
    public boolean isEmpty() {
    	return (front == -1 && rear == -1);
    }

	/**
	 * Reset the array: all elements are removed (set to 0)
	 */
    public void reset() {
    	for(int i=0;i<this.maxSize;i++) {
    		this.A[i]=null;
    	}
        front = -1;
        rear = -1;
    }

	/**
	 * Returns a string representing the elements stored in the queue
	 */
    public String toString() {
        return "Queue [front=" + front + ", rear=" + rear + ", size=" + maxSize
                + ", queue=" + Arrays.toString(A) + "]";

    }

}
