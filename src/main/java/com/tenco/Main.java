package com.tenco;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.

import com.tenco.util.DBConnectionManager;
import com.tenco.view.StoreView;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // StoreView 실행 → 프로그램 시작
        StoreView view = new StoreView();

        // try-finally 패턴:
        // try 블록에서 정상 종료되든, 예외가 발생하든
        // finally 블록은 반드시 실행된다.
        try {
            view.start();
        } finally {
            // 프로그램 종료 시 커넥션 풀 자원 해제
            // 호출하지 않으면 JVM 종료 후에도 커넥션이 남아 있을 수 있음
            DBConnectionManager.close();
        }
    }
}