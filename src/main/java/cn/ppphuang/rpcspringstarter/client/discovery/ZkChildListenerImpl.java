package cn.ppphuang.rpcspringstarter.client.discovery;

import cn.ppphuang.rpcspringstarter.client.cache.ServerDiscoveryCache;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;

import java.util.List;

/**
 * @Author: ppphuang
 * @Create: 2021/9/8
 */
@Slf4j
public class ZkChildListenerImpl implements IZkChildListener {
    /**
     * 监听子节点的删除和新增事件
     *
     * @param parentPath
     * @param childList
     * @throws Exception
     */
    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        log.info("child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
        //变化就清空缓存
        String[] arr = parentPath.split("/");
        ServerDiscoveryCache.removeAll(arr[2]);
    }
}
