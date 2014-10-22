

package com.mxh.ftp.util;

import it.sauronsoftware.ftp4j.FTPFile;

import java.util.Comparator;
import java.util.HashMap;

public class FileSortHelper {

    public enum SortMethod {
        name, size, date, type
    }

    private SortMethod mSort;

    private boolean mFileFirst;

    private HashMap<SortMethod, Comparator> mComparatorList = new HashMap<SortMethod, Comparator>();

    public FileSortHelper() {
        mSort = SortMethod.name;
        mComparatorList.put(SortMethod.name, cmpName);
        mComparatorList.put(SortMethod.size, cmpSize);
        mComparatorList.put(SortMethod.date, cmpDate);
        mComparatorList.put(SortMethod.type, cmpType);
    }

    public void setSortMethog(SortMethod s) {
        mSort = s;
    }

    public SortMethod getSortMethod() {
        return mSort;
    }

    public void setFileFirst(boolean f) {
        mFileFirst = f;
    }

    public Comparator getComparator() {
        return mComparatorList.get(mSort);
    }

    private abstract class FileComparator implements Comparator<FTPFile> {

        @Override
        public int compare(FTPFile object1, FTPFile object2) {
            if (object1.getType()==FTPFile.TYPE_DIRECTORY&&FTPFile.TYPE_DIRECTORY == object2.getType()) {
                return doCompare(object1, object2);
            }
            else if(object1.getType()==FTPFile.TYPE_FILE&&FTPFile.TYPE_FILE == object2.getType()){
            	return doCompare(object1, object2);
            }
            if (mFileFirst) {
                // the files are listed before the dirs
                return (object1.getType()==FTPFile.TYPE_DIRECTORY ? 1 : -1);
            } else {
                // the dir-s are listed before the files
                return object1.getType()==FTPFile.TYPE_DIRECTORY? -1 : 1;
            }
        }

        protected abstract int doCompare(FTPFile object1, FTPFile object2);
    }

    private Comparator cmpName = new FileComparator() {
        @Override
        public int doCompare(FTPFile object1, FTPFile object2) {
            return object1.getName().compareToIgnoreCase(object2.getName());
        }
    };

    private Comparator cmpSize = new FileComparator() {
        @Override
        public int doCompare(FTPFile object1, FTPFile object2) {
            return longToCompareInt(object1.getSize() - object2.getSize());
        }
    };

    private Comparator cmpDate = new FileComparator() {
        @Override
        public int doCompare(FTPFile object1, FTPFile object2) {
            return longToCompareInt(object2.getModifiedDate().getTime() - object1.getModifiedDate().getTime());
        }
    };

    private int longToCompareInt(long result) {
        return result > 0 ? 1 : (result < 0 ? -1 : 0);
    }

    private Comparator cmpType = new FileComparator() {
        @Override
        public int doCompare(FTPFile object1, FTPFile object2) {
            int result = Util.getExtFromFilename(object1.getName()).compareToIgnoreCase(
                    Util.getExtFromFilename(object2.getName()));
            if (result != 0)
                return result;

            return Util.getNameFromFilename(object1.getName()).compareToIgnoreCase(
                    Util.getNameFromFilename(object2.getName()));
        }
    };
}
