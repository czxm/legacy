package com.intel.cedar.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import com.intel.xml.rss.util.Pair;

/**
 * FolderIterator is a useful tool to iterator over a directory tree.
 * <p/>
 * The usage model is quite simple. There are some parameter you can customize.
 * First is the iterator type, either DFS or BFS. Second is the iterator filter,
 * which is of type FileFilter.
 * 
 * @author Shen, Han
 */
public class FolderIterator {

    public static final int DFS_ITERATOR = 0;

    public static final int BFS_ITERATOR = 1;

    private int iteratorType;

    private int maxIterationDepth = -1;

    private FileFilter filter = null;

    private File root = null;

    private FileListComparator fileListComparator = null;

    private LinkedList<File> linkedList = new LinkedList<File>();

    private LinkedList<Pair<Integer, Integer>> levelItems = new LinkedList<Pair<Integer, Integer>>();

    /**
     * Creat an iterator with DFS behavior.
     */
    public FolderIterator() {
        this(DFS_ITERATOR, -1, null);
    }

    /**
     * Creat an iterator with DFS behavior and only iterator those that could
     * pass the specified filter.
     * 
     * @param filter
     *            the filter to be used when iterating
     */
    public FolderIterator(FileFilter filter) {
        this(DFS_ITERATOR, -1, filter);
    }

    /**
     * Create an iterator with the specified iterate type(BFS or DFS), and with
     * the specified filter and max iteration depth.
     * 
     * @param iteratorType
     *            the iterator type, the value must be DFS_ITERATOR or
     *            BFS_ITERATOR
     * @param maxIterationDepth
     *            specifiy the maxdepth this iteration can ever reach
     * @param filter
     *            the filter to be applied when iterating
     */
    public FolderIterator(int iteratorType, int maxIterationDepth,
            FileFilter filter) {
        this.iteratorType = iteratorType;
        this.maxIterationDepth = maxIterationDepth;
        this.filter = filter;
        this.fileListComparator = new FileListComparator();
    }

    public void init(File rootDir) {
        linkedList.clear();
        this.root = rootDir;
        if (root != null && root.exists() && root.isDirectory()) {
            File[] files = root.listFiles(filter);
            Arrays.sort(files, fileListComparator);
            linkedList.addAll(Arrays.asList(files));
            levelItems.addFirst(new Pair<Integer, Integer>(files.length, 1));
        }
    }

    public void setIteratorType(int iteratorType) {
        this.iteratorType = iteratorType;
        this.fileListComparator = new FileListComparator();
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    public void setMaxIterationDepth(int mip) {
        this.maxIterationDepth = mip;
    }

    public boolean hasNext() {
        return !linkedList.isEmpty();
    }

    public File next() {
        File item = linkedList.removeFirst();
        Pair<Integer, Integer> pair = levelItems.getFirst();
        assert (pair.a >= 1);
        pair.a = pair.a - 1; // we do not have --pair.a, because pair.a
        // is an Integer, which is in nature an
        // immutable
        // actually what this statement does is:
        // pair.a = new Integer( pair.a.intValue() - 1 );
        // here just use the autoboxing feature of tiger
        if (pair.a == 0) {
            levelItems.removeFirst();
        }
        if (item.isDirectory()
                && (maxIterationDepth <= 0 || pair.b < maxIterationDepth)) {
            File[] files = item.listFiles(filter);
            Arrays.sort(files, fileListComparator);
            if (iteratorType == DFS_ITERATOR) {
                linkedList.addAll(0, Arrays.asList(files));
                levelItems.addFirst(new Pair<Integer, Integer>(files.length,
                        pair.b + 1));
            } else {
                levelItems.addLast(new Pair<Integer, Integer>(files.length,
                        pair.b + 1));
                int insertPosition = linkedList.size();
                linkedList.addAll(insertPosition, Arrays.asList(files));
            }
        }
        return item;
    }

    public static void main(String[] args) {
        FolderIterator iterator = new FolderIterator();
        iterator.setIteratorType(BFS_ITERATOR);
        iterator.setMaxIterationDepth(-1);
        iterator.init(new File("D:/temp"));
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}

class FileListComparator implements Comparator<File> {

    public int compare(File f1, File f2) {
        assert (f1 != null && f2 != null);
        int a = f1.isDirectory() ? 1 : 0;
        int b = f2.isDirectory() ? 1 : 0;
        return (a - b);
    }

}
