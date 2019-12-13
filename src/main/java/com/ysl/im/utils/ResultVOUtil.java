package com.ysl.im.utils;

import com.ysl.im.vo.ResultVO;

public class ResultVOUtil {

    public static ResultVO succes(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(200);
        resultVO.setMsg("成功");
        resultVO.setData(object);
        return resultVO;
    }

    public static ResultVO fail() {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(-1);
        resultVO.setMsg("失败");
        return resultVO;
    }

    public static ResultVO fail(Integer code, String msg) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        return resultVO;
    }
}
