package com.afollestad.cabinet.cab;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.cabinet.R;
import com.afollestad.cabinet.cab.base.BaseFileCab;
import com.afollestad.cabinet.file.base.File;
import com.afollestad.cabinet.sftp.SftpClient;
import com.afollestad.cabinet.ui.DrawerActivity;
import com.afollestad.cabinet.zip.Unzipper;
import com.afollestad.cabinet.zip.Zipper;

import java.util.List;

public class MainCab extends BaseFileCab {

    public MainCab() {
        super();
    }

    @Override
    public CharSequence getTitle() {
        if (getFiles().size() == 1)
            return getFiles().get(0).getName();
        return getContext().getString(R.string.x_files, getFiles().size());
    }

    @Override
    public final int getMenu() {
        return R.menu.main_cab;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        boolean showUnzip = true;
        for (File fi : getFiles()) {
            if (!fi.getExtension().equals("zip")) {
                showUnzip = false;
            }
        }
        menu.findItem(R.id.zip).setTitle(showUnzip ? R.string.unzip : R.string.zip);
        return super.onPrepareActionMode(actionMode, menu);
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.copy) {
            ((DrawerActivity) getContext()).setFileCab((BaseFileCab) new CopyCab()
                    .setFragment(getFragment()).setFiles(getFiles()).start());
            return super.onActionItemClicked(actionMode, menuItem);
        } else if (menuItem.getItemId() == R.id.cut) {
            ((DrawerActivity) getContext()).setFileCab((BaseFileCab) new CutCab()
                    .setFragment(getFragment()).setFiles(getFiles()).start());
            return super.onActionItemClicked(actionMode, menuItem);
        } else if (menuItem.getItemId() == R.id.delete) {
            deleteNextFile();
            return super.onActionItemClicked(actionMode, menuItem);
        } else if (menuItem.getItemId() == R.id.selectAll) {
            List<File> newSelected = getFragment().getAdapter().checkAll();
            addFiles(newSelected);
            invalidate();
            return true;
        } else if (menuItem.getItemId() == R.id.zip) {
            if (menuItem.getTitle().toString().equals(getContext().getString(R.string.unzip))) {
                Unzipper.unzip(getFragment(), getFiles(), new Zipper.ZipCallback() {
                    @Override
                    public void onComplete() {
                        MainCab.super.onActionItemClicked(actionMode, menuItem);
                    }
                });
            } else {
                Zipper.zip(getFragment(), getFiles(), new Zipper.ZipCallback() {
                    @Override
                    public void onComplete() {
                        MainCab.super.onActionItemClicked(actionMode, menuItem);
                    }
                });
            }
            return true;
        }
        return false;
    }

    private void deleteNextFile() {
        if (getFiles().size() == 0) {
            invalidate();
            return;
        }
        getFiles().get(0).delete(new SftpClient.CompletionCallback() {
            @Override
            public void onComplete() {
                getFragment().getAdapter().remove(getFiles().get(0));
                getFiles().remove(0);
                deleteNextFile();
            }

            @Override
            public void onError(Exception e) {
                // Nothing is needed here
            }
        });
    }
}
