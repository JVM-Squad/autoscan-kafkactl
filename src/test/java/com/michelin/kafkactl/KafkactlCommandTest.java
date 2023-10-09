package com.michelin.kafkactl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkactlCommandTest {
    @InjectMocks
    private KafkactlCommand kafkactlCommand;

    @Test
    void shouldSetVerbose() {
        CommandLine cmd = new CommandLine(kafkactlCommand);
        int code = cmd.execute("-v");
        assertEquals(0, code);
        assertTrue(kafkactlCommand.verbose);
    }

    @Test
    void shouldNotSetVerbose() {
        CommandLine cmd = new CommandLine(kafkactlCommand);
        int code = cmd.execute();
        assertEquals(0, code);
        assertFalse(kafkactlCommand.verbose);
    }
}
