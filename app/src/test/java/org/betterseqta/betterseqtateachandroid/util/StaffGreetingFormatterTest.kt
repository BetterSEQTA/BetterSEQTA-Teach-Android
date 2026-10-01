package org.betterseqta.betterseqtateachandroid.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StaffGreetingFormatterTest {

    @Test
    fun formatStaffGreetingName_usesTitleAndSurname() {
        assertEquals("Mr Teach", formatStaffGreetingName("Mr Teach Tester1"))
        assertEquals("Ms Smith", formatStaffGreetingName("Ms Smith Jones"))
        assertEquals("Dr Ng", formatStaffGreetingName("Dr. Ng Example"))
    }

    @Test
    fun formatStaffGreetingName_avoidsTitleOnly() {
        assertNull(formatStaffGreetingName("Mr"))
    }

    @Test
    fun formatStaffGreetingName_firstNameWhenNoTitle() {
        assertEquals("Alex", formatStaffGreetingName("Alex Johnson"))
    }
}
