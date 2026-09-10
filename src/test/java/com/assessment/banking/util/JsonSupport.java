package com.assessment.banking.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;

public final class JsonSupport {

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    private static final Set<String> TYPE_FIELDS =
            Set.of(
                    "type",
                    "txType",
                    "transactionType",
                    "transaction_type");

    private static final Set<String> AMOUNT_FIELDS =
            Set.of(
                    "amount",
                    "txAmount",
                    "transactionAmount",
                    "transferAmount");

    private static final Set<String> STATUS_FIELDS =
            Set.of(
                    "status",
                    "txStatus",
                    "transactionStatus");

    private JsonSupport() {
    }

    public static String text(
            Response response,
            String... fieldNames) {

        JsonNode node =
                findFirst(parse(response), fieldNames);

        if (node == null || node.isNull()) {
            throw new AssertionError(
                    "Could not find any of these fields: "
                            + String.join(", ", fieldNames)
                            + System.lineSeparator()
                            + "Response: "
                            + response.asString());
        }

        return node.asText();
    }

    public static BigDecimal decimal(
            Response response,
            String... fieldNames) {

        String value = text(response, fieldNames);

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new AssertionError(
                    "Expected a numeric monetary value but found: "
                            + value,
                    e);
        }
    }

    public static int countSuccessfulTransactions(
            Response response,
            String expectedType,
            BigDecimal expectedAmount) {

        JsonNode root = parse(response);

        List<JsonNode> objects =
                new ArrayList<>();

        collectObjects(root, objects);

        int count = 0;

        for (JsonNode object : objects) {

            JsonNode typeNode =
                    findDirect(
                            object,
                            TYPE_FIELDS.toArray(String[]::new));

            JsonNode amountNode =
                    findDirect(
                            object,
                            AMOUNT_FIELDS.toArray(String[]::new));

            if (typeNode == null || amountNode == null) {
                continue;
            }

            String actualType =
                    typeNode.asText()
                            .toUpperCase(Locale.ROOT);

            if (!actualType.contains(
                    expectedType.toUpperCase(Locale.ROOT))) {
                continue;
            }

            BigDecimal actualAmount;

            try {
                actualAmount =
                        new BigDecimal(
                                amountNode.asText());
            } catch (NumberFormatException e) {
                continue;
            }

            if (actualAmount.compareTo(expectedAmount) != 0) {
                continue;
            }

            if (isSuccessfulOrUnspecified(object)) {
                count++;
            }
        }

        return count;
    }

    private static JsonNode parse(
            Response response) {

        String body = response == null
                ? null
                : response.asString();

        if (body == null || body.isBlank()) {
            throw new AssertionError(
                    "Expected JSON response body but body was empty.");
        }

        try {
            return MAPPER.readTree(body);
        } catch (Exception e) {
            throw new AssertionError(
                    "Response body is not valid JSON: "
                            + body,
                    e);
        }
    }

    private static JsonNode findFirst(
            JsonNode node,
            String... fieldNames) {

        if (node == null) {
            return null;
        }

        JsonNode direct =
                findDirect(node, fieldNames);

        if (direct != null) {
            return direct;
        }

        if (node.isObject()) {

            Iterator<JsonNode> iterator =
                    node.elements();

            while (iterator.hasNext()) {

                JsonNode found =
                        findFirst(
                                iterator.next(),
                                fieldNames);

                if (found != null) {
                    return found;
                }
            }
        }

        if (node.isArray()) {

            for (JsonNode child : node) {

                JsonNode found =
                        findFirst(
                                child,
                                fieldNames);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private static JsonNode findDirect(
            JsonNode node,
            String... fieldNames) {

        if (node == null || !node.isObject()) {
            return null;
        }

        for (String requested : fieldNames) {

            Iterator<Map.Entry<String, JsonNode>> fields =
                    node.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> field =
                        fields.next();

                if (field.getKey()
                        .equalsIgnoreCase(requested)) {

                    return field.getValue();
                }
            }
        }

        return null;
    }

    private static boolean isSuccessfulOrUnspecified(
            JsonNode object) {

        JsonNode statusNode =
                findDirect(
                        object,
                        STATUS_FIELDS.toArray(String[]::new));

        if (statusNode == null || statusNode.isNull()) {
            return true;
        }

        String status =
                statusNode.asText()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        return !status.contains("FAIL")
                && !status.contains("REJECT")
                && !status.contains("DECLIN")
                && !status.contains("ERROR");
    }

    private static void collectObjects(
            JsonNode node,
            List<JsonNode> output) {

        if (node == null) {
            return;
        }

        if (node.isObject()) {

            output.add(node);

            Iterator<JsonNode> iterator =
                    node.elements();

            while (iterator.hasNext()) {
                collectObjects(
                        iterator.next(),
                        output);
            }

        } else if (node.isArray()) {

            for (JsonNode child : node) {
                collectObjects(child, output);
            }
        }
    }
}
