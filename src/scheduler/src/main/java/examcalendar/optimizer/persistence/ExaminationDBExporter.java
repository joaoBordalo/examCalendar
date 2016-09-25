package examcalendar.optimizer.persistence;

import examcalendar.optimizer.domain.Exam;
import examcalendar.optimizer.domain.Examination;
import examcalendar.optimizer.domain.Period;
import examcalendar.optimizer.domain.RoomPeriod;
import org.optaplanner.examples.common.persistence.AbstractSolutionExporter;
import org.optaplanner.examples.common.persistence.SolutionDao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

/**
 * Created by Gustavo on 15/07/2016.
 */
public class ExaminationDBExporter extends AbstractSolutionExporter<Examination> {
    public ExaminationDBExporter(SolutionDao solutionDao) {
        super(solutionDao);
    }

    public ExaminationDBExporter(boolean withoutDao) {
        super(withoutDao);
    }

    @Override
    public String getOutputFileSuffix() {
        return null;
    }

    @Override
    public void writeSolution(Examination examination, File outputFile) {
        // File contents: Request ID
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFile);
            fos.write(examination.getId());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void writeSolution(Examination examination, int requestID) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/examcalendar?serverTimezone=UTC", "root", ""); // TODO (hardcoded)
            writeExamRooms(requestID, conn, examination);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void writeExamRooms(int creator, Connection conn, Examination examination) throws SQLException {
        for (Exam exam : examination.getExamList()) {
            Period period = null;
            for (RoomPeriod rp : examination.getRoomPeriodList()) {
                if (rp.getExam() == null || !rp.getExam().equals(exam)) {
                    continue;
                }
                if (period == null) {
                    period = rp.getPeriod();
                } else if (rp.getPeriod() != period) {
                    continue;
                }
                PreparedStatement ps = conn.prepareStatement("INSERT INTO examrooms (exam, room) VALUES (?, ?)");
                ps.setInt(1, exam.getId());
                ps.setInt(2, rp.getRoom().getId());
                ps.execute();
            }
            PreparedStatement ps = conn.prepareStatement("UPDATE exams SET day = ?, time = ? WHERE id = ?");
            if (period == null) {
                ps.setNull(1, Types.DATE);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setDate(1, new java.sql.Date(period.getDate().getTime()));
                ps.setInt(2, period.getTime().ordinal());
            }
            ps.setInt(3, exam.getId());
            ps.execute();
        }
    }
}
