package com.example.fw.web.page;

import org.springframework.data.domain.Page;
import org.thymeleaf.util.NumberUtils;

/**
 * 
 * ページネーションのページ情報を出力するクラス 
 *
 */
public class PageInfo {
	/**
	 * ページ番号のリスト
	 * @param page
	 * @param pageLinkMaxDispNum
	 * @return
	 */
	public Integer[] sequence(Page<?> page, int pageLinkMaxDispNum) {
     
        int begin = Math.max(1, page.getNumber() + 1 - pageLinkMaxDispNum / 2);
        int end = begin + (pageLinkMaxDispNum - 1);
        if (end > page.getTotalPages() - 1) {
            end = page.getTotalPages();
            begin = Math.max(1, end - (pageLinkMaxDispNum - 1));
        }
     
        return NumberUtils.sequence(begin, end);
    }
}
