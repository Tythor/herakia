package tythor.herakia.hazelcast.management.component;

import com.hazelcast.cluster.Member;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import tythor.herakia.hazelcast.management.callable.TimestampRunnable;
import tythor.herakia.hazelcast.management.callback.TimestampCallback;
import tythor.herakia.hazelcast.management.dto.DistributedObjectDto;
import tythor.herakia.hazelcast.management.dto.DistributedObjectStats;
import tythor.herakia.hazelcast.management.dto.HazelcastStats;
import tythor.herakia.hazelcast.management.dto.PingStats;
import tythor.herakia.hazelcast.management.enumeration.DistributedObjectType;
import tythor.herakia.hazelcast.management.enumeration.QueryType;
import tythor.herakia.utility.HazelcastUtil;
import tythor.herakia.utility.thread.ThreadStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HazelcastStatsCollector {
    private boolean isPingInit = false;

    private final HzMapService hzMapService;

    public HazelcastStats getHazelcastStats(QueryType queryType) {
        List<Member> members = queryType == QueryType.LOCAL
            ? List.of(HazelcastUtil.getInstance().getCluster().getLocalMember())
            : HazelcastUtil.getInstance().getCluster().getMembers().stream().toList();

        HazelcastStats stats = new HazelcastStats();
        stats.setDistributedObjectDtos(getDistributedObjectInfoList(queryType));
        stats.setPingStatsList(getPingStats(members));
        stats.setMemberCount(stats.getPingStatsList().size());

        return stats;
    }

    public List<DistributedObjectDto> getDistributedObjectInfoList(QueryType queryType) {
        List<Member> members = queryType == QueryType.LOCAL
            ? List.of(HazelcastUtil.getInstance().getCluster().getLocalMember())
            : HazelcastUtil.getInstance().getCluster().getMembers().stream().toList();

        return queryType == QueryType.CLUSTER
            ? List.of(getDistributedObjectInfoCluster(members))
            : members.stream().map(this::getDistributedObjectInfoList).toList();
    }

    public List<PingStats> getPingStats(List<Member> members) {
        if (!isPingInit) {
            isPingInit = true;
            getPingStats(members);
        }

        IExecutorService executorService = HazelcastUtil.getInstance().getExecutorService("defaultExecutorService");

        TimestampCallback callback = new TimestampCallback();
        executorService.submitToMembers(new TimestampRunnable(), members, callback);

        return callback.acquirePingStats();
    }

    private DistributedObjectDto getDistributedObjectInfoList(Member member) {
        Collection<DistributedObject> distributedObjects = HazelcastUtil.getInstance().getDistributedObjects();

        List<DistributedObjectStats> distributedObjectStatsList = new ArrayList<>();
        for (DistributedObject distributedObject : distributedObjects) {
            String mapName = ThreadStorage.get("mapName");
            if (!mapName.equals(distributedObject.getName())) continue;

            DistributedObjectStats distributedObjectStats = getDistributedObjectStats(member, distributedObject);
            if (distributedObjectStats != null) {
                distributedObjectStatsList.add(distributedObjectStats);
            }
        }

        DistributedObjectDto distributedObjectDto = new DistributedObjectDto();
        distributedObjectDto.setDistributedObjectStatsList(distributedObjectStatsList);
        distributedObjectDto.setOwner(member.getAttribute("hostname"));
        return distributedObjectDto;
    }

    private DistributedObjectDto getDistributedObjectInfoCluster(List<Member> members) {
        Map<Pair<DistributedObjectType, String>, DistributedObjectStats> distributedObjectStatsMap = new HashMap<>();
        Collection<DistributedObject> distributedObjects = HazelcastUtil.getInstance().getDistributedObjects();

        for (Member member : members) {
            for (DistributedObject distributedObject : distributedObjects) {
                String mapName = ThreadStorage.get("mapName");
                if (!mapName.equals(distributedObject.getName())) continue;

                DistributedObjectStats distributedObjectStats = getDistributedObjectStats(member, distributedObject);
                if (distributedObjectStats != null) {
                    Pair<DistributedObjectType, String> key = Pair.of(distributedObjectStats.getType(), distributedObjectStats.getName());
                    distributedObjectStatsMap.merge(key, distributedObjectStats, DistributedObjectStats::mergeDistributedObjectStats);
                }
            }
        }

        DistributedObjectDto distributedObjectDto = new DistributedObjectDto();
        distributedObjectDto.setDistributedObjectStatsList(distributedObjectStatsMap.values().stream().sorted(Comparator.comparing(DistributedObjectStats::getSize).reversed()).toList());
        distributedObjectDto.setOwner(QueryType.CLUSTER.name());
        return distributedObjectDto;
    }

    private DistributedObjectStats getDistributedObjectStats(Member member, DistributedObject object) {
        if (IMap.class.isAssignableFrom(object.getClass())) {
            return hzMapService.getMapStats(member, object.getName());
        } else if (IQueue.class.isAssignableFrom(object.getClass())) {
            //TODO: Implement support for other types here
        } else {
            log.debug("Skipping unsupported distributed object type: {}", object.getName());
        }
        return null;
    }
}
