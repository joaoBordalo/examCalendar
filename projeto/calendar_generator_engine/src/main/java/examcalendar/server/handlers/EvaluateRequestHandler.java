package examcalendar.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import examcalendar.optimizer.domain.Examination;
import examcalendar.optimizer.persistence.ExaminationDBImporter;
import org.json.JSONException;
import org.json.JSONObject;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Gustavo on 10/07/2016.
 */
public class EvaluateRequestHandler extends AbstractRequestHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            try {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    Map<String, String> params = parseQueryParams(exchange.getRequestURI());
                    String creator = params.get("creator");
                    if (creator == null) {
                        JSONObject data = new JSONObject();
                        data.put("creator", "Missing creator ID.");
                        throw new RequestHandlerFailException(400, data);
                    }
                    int creatorID;
                    try {
                        creatorID = Integer.parseInt(params.get("creator"));
                    } catch (NumberFormatException e) {
                        JSONObject data = new JSONObject();
                        data.put("creator", "Creator ID is invalid.");
                        throw new RequestHandlerFailException(400, data);
                    }
                    Examination solution = new ExaminationDBImporter(true).readSolution(creatorID);
                    evaluateSolution(solution);
                    this.sendSuccessResponse(exchange, JSONObject.NULL, 200);
                } else {
                    // Method not allowed
                    JSONObject data = new JSONObject();
                    data.put("method", "Method \"" + method + "\" not allowed.");
                    exchange.getResponseHeaders().add("Allow", "GET");
                    throw new RequestHandlerFailException(405, data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RequestHandlerErrorException(500, "Unknown error.");
            }
        } catch (RequestHandlerException e) {
            e.send(exchange);
        }
    }

    public static JSONObject evaluateSolution(Solution solution) {
        SolverFactory solverFactory = SolverFactory.createFromXmlResource("examinationSolverConfig.xml");
        Solver solver = solverFactory.buildSolver();
        ScoreDirectorFactory scoreDirectorFactory = solver.getScoreDirectorFactory();
        ScoreDirector guiScoreDirector = scoreDirectorFactory.buildScoreDirector();
        guiScoreDirector.setWorkingSolution(solution);
        HardSoftScore score = (HardSoftScore) guiScoreDirector.calculateScore();
        JSONObject jsonScore = new JSONObject();
        try {
            jsonScore.put("hard", score.getHardScore());
            jsonScore.put("soft", score.getSoftScore());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
        System.out.println(jsonScore);
        for (ConstraintMatchTotal constraintMatchTotal : guiScoreDirector.getConstraintMatchTotals()) {
            String constraintName = constraintMatchTotal.getConstraintName();
            Number weightTotal = constraintMatchTotal.getWeightTotalAsNumber();
            for (ConstraintMatch constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
                List<Object> justificationList = constraintMatch.getJustificationList();
                Number weight = constraintMatch.getWeightAsNumber();
                System.out.println(justificationList.get(0) + " --- " + constraintMatch.getConstraintName() + " --- " + weight);
            }
        }
        return jsonScore; // TODO
    }
}