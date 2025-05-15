package com.example.hcm25_cpl_ks_java_01_lms.user;

import com.example.hcm25_cpl_ks_java_01_lms.role.Role;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserExcelImporter {
    public static List<User> importUsers(InputStream inputStream) throws IOException {
        List<User> users = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            // Skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            Iterator<Cell> cellsInRow = currentRow.iterator();

            User user = new User();
            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                switch (cellIdx) {
                    case 0:
//                        user.setId((long) currentCell.getNumericCellValue());
                        break;
                    case 1:
                        user.setUsername(currentCell.getStringCellValue());
                        break;
                    case 2:
                        user.setPassword(currentCell.getStringCellValue());
                        break;
                    case 3:
                        user.setFirstName(currentCell.getStringCellValue());
                        break;
                    case 4:
                        user.setLastName(currentCell.getStringCellValue());
                        break;
                    case 5:
                        user.setEmail(currentCell.getStringCellValue());
                        break;
                    case 6:
                        user.setIs2faEnabled(currentCell.getBooleanCellValue());
                        break;
                    case 7:
                        user.setIsLocked(currentCell.getBooleanCellValue());
                        break;
                    case 8:
                        List<Role> roles = new ArrayList<>();
                        String[] roleNames = currentCell.getStringCellValue().split(",");
                        for (String roleName : roleNames) {
                            Role role = new Role();
                            role.setName(roleName.trim());
                            roles.add(role);
                        }
                        user.setRoles(roles);
                        break;
                    default:
                        break;
                }
                cellIdx++;
            }

            users.add(user);
        }

        workbook.close();
        return users;
    }
}
