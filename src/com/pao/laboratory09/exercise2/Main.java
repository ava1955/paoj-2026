package com.pao.laboratory09.exercise2;

import com.pao.laboratory09.exercise1.TipTranzactie;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    private static final String OUTPUT_FILE = "output/lab09_ex2.bin";
    private static final int RECORD_SIZE = 32;

    public static void main(String[] args) throws Exception {
        File file = new File("output");
        if (!file.exists()) file.mkdirs();

        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextInt()) return;

        int n = scanner.nextInt();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(OUTPUT_FILE))) {
            for (int i = 0; i < n; i++) {
                int id = scanner.nextInt();
                double suma = scanner.nextDouble();
                String data = scanner.next();
                TipTranzactie tip = TipTranzactie.valueOf(scanner.next());

                dos.write(serializeTransaction(id, suma, data, tip));
            }
        }

        try (RandomAccessFile raf = new RandomAccessFile(OUTPUT_FILE, "rw")) {
            while (scanner.hasNext()) {
                String command = scanner.next();
                switch (command) {
                    case "READ":
                        int readIdx = scanner.nextInt();
                        printTransaction(raf, readIdx);
                        break;

                    case "UPDATE":
                        int updateIdx = scanner.nextInt();
                        String statusStr = scanner.next();
                        updateStatus(raf, updateIdx, statusStr);
                        break;

                    case "PRINT_ALL":
                        long numRecords = raf.length() / RECORD_SIZE;
                        for (int i = 0; i < numRecords; i++) {
                            printTransaction(raf, i);
                        }
                        break;
                }
            }
        }
    }

    private static byte[] serializeTransaction(int id, double suma, String data, TipTranzactie tip) {
        ByteBuffer buffer = ByteBuffer.allocate(RECORD_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(id);
        buffer.putDouble(suma);
        byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);
        byte[] paddedData = new byte[10];
        Arrays.fill(paddedData, (byte) ' ');
        System.arraycopy(dataBytes, 0, paddedData, 0, Math.min(dataBytes.length, 10));
        buffer.put(paddedData);

        buffer.put((byte) (tip == TipTranzactie.CREDIT ? 0 : 1));
        buffer.put((byte) 0);
        return buffer.array();
    }

    private static void updateStatus(RandomAccessFile raf, int idx, String statusStr) throws IOException {
        byte statusCode = switch (statusStr) {
            case "PROCESSED" -> (byte) 1;
            case "REJECTED" -> (byte) 2;
            default -> (byte) 0;
        };
        raf.seek((long) idx * RECORD_SIZE + 23);
        raf.write(statusCode);
        System.out.println("Updated [" + idx + "]: " + statusStr);
    }

    private static void printTransaction(RandomAccessFile raf, int idx) throws IOException {
        byte[] bytes = new byte[RECORD_SIZE];
        raf.seek((long) idx * RECORD_SIZE);
        raf.readFully(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        int id = buffer.getInt();
        double suma = buffer.getDouble();

        byte[] dataBytes = new byte[10];
        buffer.get(dataBytes);
        String data = new String(dataBytes, StandardCharsets.US_ASCII).trim();

        int tipByte = buffer.get();
        String tip = (tipByte == 0) ? "CREDIT" : "DEBIT";

        int statusByte = buffer.get();
        String status = switch (statusByte) {
            case 1 -> "PROCESSED";
            case 2 -> "REJECTED";
            default -> "PENDING";
        };

        System.out.printf("[%d] id=%d data=%s tip=%s suma=%.2f RON status=%s%n",
                idx, id, data, tip, suma, status);
    }
}