package com.interview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.interview.common.exception.BusinessException;
import com.interview.dto.CreateJdDTO;
import com.interview.entity.JobDescriptions;
import com.interview.mapper.JdMapper;
import com.interview.service.JdService;
import com.interview.vo.JdVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
@Service
public class JdServiceImpl implements JdService {

    /**
     * 每个用户最多创建 JD 数量
     */
    private static final int MAX_JD_COUNT = 20;

    @Resource
    private JdMapper jdMapper;

    /**
     * 创建 JD
     */
    @Override
    public JdVO createJd(CreateJdDTO dto) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. content 和 sourceUrl 至少填一个
        if (!StringUtils.hasText(dto.getContent()) && !StringUtils.hasText(dto.getSourceUrl())) {
            throw new BusinessException("JD 内容和来源链接至少填写一个");
        }

        // 2. 校验数量上限
        int count = jdMapper.countByUserId(userId);
        if (count >= MAX_JD_COUNT) {
            throw new BusinessException("JD 数量已达上限（最多" + MAX_JD_COUNT + "个）");
        }

        // 3. 插入数据库
        JobDescriptions jd = JobDescriptions.builder()
                .userId(userId)
                .title(dto.getTitle())
                .companyName(dto.getCompanyName())
                .sourceUrl(StringUtils.hasText(dto.getSourceUrl()) ? dto.getSourceUrl() : null)
                .content(StringUtils.hasText(dto.getContent()) ? dto.getContent() : null)
                .build();
        jdMapper.insert(jd);

        // 4. 查询完整记录并返回
        JobDescriptions savedJd = jdMapper.selectById(jd.getId());
        JdVO jdVO = new JdVO();
        BeanUtils.copyProperties(savedJd, jdVO);
        return jdVO;
    }

    /**
     * 获取当前用户的 JD 列表
     */
    @Override
    public List<JdVO> getJdList() {
        long userId = StpUtil.getLoginIdAsLong();
        List<JobDescriptions> list = jdMapper.selectByUserId(userId);
        return list.stream().map(jd -> {
            JdVO jdVO = new JdVO();
            BeanUtils.copyProperties(jd, jdVO);
            return jdVO;
        }).toList();
    }

    /**
     * 获取 JD 详情
     */
    @Override
    public JdVO getJdDetail(Long id) {
        long userId = StpUtil.getLoginIdAsLong();

        JobDescriptions jd = jdMapper.selectById(id);
        if (jd == null) {
            throw new BusinessException("JD 不存在");
        }
        if (!jd.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        JdVO jdVO = new JdVO();
        BeanUtils.copyProperties(jd, jdVO);
        return jdVO;
    }

    /**
     * 删除 JD
     */
    @Override
    public void deleteJd(Long id) {
        long userId = StpUtil.getLoginIdAsLong();

        int rows = jdMapper.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            throw new BusinessException("JD 不存在或无权限删除");
        }
    }

    /**
     * 批量删除 JD
     */
    @Override
    public void deleteJdByIds(List<Long> ids) {

        // 1. 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除列表不能为空");
        }

        long userId = StpUtil.getLoginIdAsLong();

        // 2. 一条 SQL 批量删除（WHERE user_id = ? AND id IN (...)）
        int rows = jdMapper.deleteByIdsAndUserId(ids, userId);
        if (rows == 0) {
            throw new BusinessException("未找到可删除的 JD");
        }
    }

}
