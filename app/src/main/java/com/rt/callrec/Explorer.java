package com.rt.callrec;

import android.content.Context;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by QNIT on 12/13/2016.
 */

public class Explorer extends File {
    public static final String
            // Call_yyyy-MM-dd_hh-mm-ss-(IN or OUT)_phoneNum(3->15 digit).final by (amr or mp3 or 3gp)
            callFilePattern = "^([cC]all)_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_(IN|OUT)_\\d{3,15}\\.(amr|mp3|3gp)$";

    public Explorer(String path) {
        super(path);
    }

    // get list subFolder of dir
    public static File[] getDirArray(String dir) {
        if (dir == null || dir.isEmpty()) return null;
        return new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
    }

    public static File[] getCallFileArray(String dir) {
        if (dir == null || dir.isEmpty()) return null;
        return new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (isCallFile(file));
            }
        });
    }

    public static boolean isCallFile(File file) {
//        if (file == null) return false;
//        return file.isFile() ? file.getName().matches(callFilePattern) : false;
        return true;
    }

    public static List<String> getListFileName(String path) {
        if (path == null) return null;

        List<String> list = new ArrayList<>();

        File[] dirs = getDirArray(path);
        if (dirs != null) {
            for (int i = dirs.length - 1; i >= 0; i--) {
                list.add("Date_" + dirs[i].getName());
                File[] files = getCallFileArray(path + dirs[i].getName() + "/");
                for (int j = files.length - 1; j >= 0; j--) {
                    list.add(files[j].getName());
                }
            }
        }
        return list;
    }

    public static List<RecFile> getRecList(Context context, String dir) {
        if (dir == null || dir.isEmpty() || context == null) return null;
/**
 * Duyệt qua từng thư mục con trong thư mục dir
 * với mỗi thư mục con, lấy tất cả file Call và:
 * nếu thư mục con trống thì xóa nó đi
 */
        List<RecFile> list = null;
//        File file = new File(dir);
//        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();

        File[] dirs = getDirArray(dir);
        if (dirs != null) {
            list = new ArrayList<>();
            for (int i = dirs.length - 1; i >= 0; i--) {
                File[] files = getCallFileArray(dirs[i].getPath());
                if (files != null)
                    if (files.length == 0) {
                        dirs[i].delete();
                    } else {
                        list.add(new RecFile(context, dirs[i].getPath()));
                        for (int j = files.length - 1; j >= 0; j--) {
                            list.add(new RecFile(context, files[j].getPath()));
                        }
                    }
            }
        }
        return list;
    }

    public static List<RecFile> restoreListRec(Context context, String dir) {
        if (dir == null || dir.isEmpty() || context == null) return null;
/**
 * Truy cập đến file listRec(lưu trữ listRec) trong thư mục dir,
 * nếu tồn tại thi read file lấy listRec
 * ngược lại, thì gọi hàm getListRec để lấy listRec và backup nó
 */
        List<RecFile> list = null;

        File file = new File(dir, "listRec");
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                list = (List<RecFile>) ois.readObject();
                ois.close();
            } catch (IOException e) {
                file.delete();
                return restoreListRec(context, dir);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if ((list = getRecList(context, dir)) != null) {
            backupListRec(list, dir);
        }
        return list;
    }

    public static boolean deleteBackupFile(String dir) {
        boolean re = false;
        File file = new File(dir, "listRec");
        if (file.exists()) re = file.delete();
        return re;
    }


    private static void backupList(File file, List list) {
        if (list == null || file == null) return;
        if (file.exists()) file.delete();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(list);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void backupListRec(List<RecFile> list, String dir) {
        if (list == null || dir == null || dir.isEmpty()) return;

        File file = new File(dir, "listRec");
        backupList(file, list);

//        if (file.exists()) file.delete();
//        try {
//            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
//            oos.writeObject(list);
//            oos.flush();
//            oos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static List<RecItem> getItemList(Context context, String dir) {
        if (context == null || dir == null | dir.isEmpty()) return null;

        List<RecItem> list = null;
        List<RecFile> lrecFile = restoreListRec(context, dir);
        if (lrecFile != null) {
            list = new ArrayList<>();
            for (RecFile rf : lrecFile) {
                list.add(new RecItem(context, rf));
            }
        }
        return list;
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) return true;

        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files == null)
                return true;
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory())
                    deleteDir(files[i]);
                else
                    files[i].delete();
            }
        }
        return dir.delete();
    }

    public static void insertRecFile(Context context, RecFile recFile) {
        if (recFile == null || context == null) return;

        List<RecFile> list = restoreListRec(context, recFile.getParentFile().getParent());
        if (list == null) list = new ArrayList<>();
        if (list.size() > 0 && list.get(0).getName().equals(DateFormat.format(context.getString(R.string.dir_name_format), new Date()))) {
            list.add(1, recFile);
        } else {
            list.add(0, recFile);
            list.add(0, new RecFile(context, recFile.getParent()));
        }
        backupListRec(list, recFile.getParentFile().getParent());
    }

    public static int getTotalRecCount(Context context, String dir) {
        if (dir == null || dir.isEmpty() || context == null) return 0;

        List<RecFile> list = restoreListRec(context, dir);
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isFile()) count++;
        }
        return count;
    }

    public boolean isEmptyDir() {
        return (this.getParentFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return isCallFile(file);
            }
        }).length == 0);
    }

    public static void deleteOdlFile(Context context, String dir) {
        List<RecFile> list = restoreListRec(context, dir);
        list.get(list.size() - 1).delete();
        list.remove(list.size() - 1);
        if (list.get(list.size() - 1).isDirectory()) {
            list.get(list.size() - 1).delete();
            list.remove(list.size() - 1);
        }
        Explorer.backupListRec(list, dir);
    }


    public static float getFreeSpaceMB(String dir) {
        File file = new File(dir);
        return file.getFreeSpace() / (1024 * 1024);
    }

    public static List<String> getLimitPhoneList(Context context) {
        File file = new File(context.getCacheDir().getPath(), "listPhoneLimit");
        return (List<String>) readList(file);
    }

    public static List<String> getAllowPhoneList(Context context) {
        File file = new File(context.getCacheDir().getPath(), "listPhoneAllow");
        return (List<String>) readList(file);
    }

    private static List readList(File file) {
        List list = null;
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                list = (List) ois.readObject();
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void backupLimitPhoneList(Context context, List list) {
        File file = new File(context.getCacheDir().getPath(), "listPhoneLimit");
        backupList(file, list);
    }

    public static void backupAllowPhoneList(Context context, List list) {
        File file = new File(context.getCacheDir().getPath(), "listPhoneAllow");
        backupList(file, list);
    }

    public static List<File> getDirList(File file) {
        return Arrays.asList(file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isDirectory() && file.canRead() && file.canWrite());
            }
        }));
    }
}
