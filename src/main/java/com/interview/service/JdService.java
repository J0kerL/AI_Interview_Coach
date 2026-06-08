package com.interview.service;

import com.interview.dto.CreateJdDTO;
import com.interview.vo.JdVO;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface JdService {

    /**
     * 创建 JD
     */
    JdVO createJd(CreateJdDTO dto);

    /**
     * 获取当前用户的 JD 列表
     */
    List<JdVO> getJdList();

    /**
     * 获取 JD 详情
     */
    JdVO getJdDetail(Long id);

    /**
     * 删除 JD
     */
    void deleteJd(Long id);

    /**
     * 批量删除 JD
     */
    void deleteJdByIds(List<Long> ids);

}
