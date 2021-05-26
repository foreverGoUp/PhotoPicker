package com.ckc.photopicker;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : 陈孔财
 *     e-mail : chenkongcai@lexiangbao.com
 *     time   : 2021/5/13
 *     desc   : 选择收集器
 *     version: 1.0
 * </pre>
 */
public class SelectionCollector {

    private int maxSelectableNum;//最大可选数量
    List<Photo> selectedItems = new ArrayList<>();
    private OnSelectChangeListener onSelectChangeListener;

    public void setOnSelectChangeListener(OnSelectChangeListener onSelectChangeListener) {
        this.onSelectChangeListener = onSelectChangeListener;
    }

    public SelectionCollector(int maxSelectableNum) {
        this.maxSelectableNum = maxSelectableNum;
    }

    public int getSelectedNum(){
        return selectedItems.size();
    }

    public int getMaxSelectableNum() {
        return maxSelectableNum;
    }

    /**
     * @return -1:取消选中，0：超过最大数量，>0：选中序号
     * */
    public int select(Photo photo){
        if (selectedItems.contains(photo)){
            selectedItems.remove(photo);
            notifySelectChanged();
            return -1;
        }else{
            int size = selectedItems.size();
            if (size == maxSelectableNum){
                return 0;
            }else {
                selectedItems.add(photo);
                notifySelectChanged();
                return size + 1;
            }
        }
    }

    public void notifySelectChanged(){
        if (onSelectChangeListener != null) onSelectChangeListener.onSelectChange(maxSelectableNum, selectedItems.size());
    }

    public int selectedIndex(Photo photo){
        if (selectedItems.contains(photo)){
            return selectedItems.indexOf(photo) + 1;
        }else {
            return -1;
        }
    }

    public interface OnSelectChangeListener{
        void onSelectChange(int maxSelectableNum, int selectedNum);
    }
}
