package webCopy

import org.restlet.data.MediaType
import org.restlet.resource.ClientResource
import org.restlet.Client
import org.restlet.data.Protocol
import org.restlet.representation.Representation
import scala.xml.PrettyPrinter
import scala.xml.Node
import scala.xml.XML
import java.io.ByteArrayOutputStream
import scala.Left
import scala.Right
import scala.xml.Elem
import scala.xml.MetaData
import edu.umro.util.Utility
import java.io.InputStream
import java.io.FileOutputStream
import java.io.File

object FixLinks {

    private val dir = new File("D:\\tmp\\wiki\\LegacySoftwareDocs\\umrosw")

    val fileList = dir.listFiles.filter(f => f.getName.endsWith(".html"))

    val fileNameList = fileList.map(f => f.getName)

    val patList = List(
        "361_37_Readiness_review_May_25_2010",
        "361_38_Release_Planning",
        "361_38_Release_Planning_-_1/11/11",
        "361_38_Release_Planning_-_1/14/11",
        "361_38_Testing_Review_Meeting",
        "Adaptive_Radiotherapy_Tools_Page",
        "Adaptive_Therapy_Bookkeeping",
        "Alan%27s_Old_Project_Page",
        "Alan%27s_Project_Page",
        "Applaundrylist",
        "AVS:API_Reference",
        "AVS:AVSadd_float_parameter",
        "AVS:AVSadd_parameter",
        "AVS:AVSadd_parameter#AVS_Parameter_Types",
        "AVS:AVSadd_parameter_prop",
        "AVS:AVSadd_parameter_prop#Parameter_Properties",
        "AVS:AVSautofree_output",
        "AVS:AVSbuild_2d_field",
        "AVS:AVSbuild_3d_field",
        "AVS:AVSbuild_field",
        "AVS:AVSchoice_number",
        "AVS:AVScolormap_get",
        "AVS:AVScolormap_set",
        "AVS:AVScommand",
        "AVS:AVSconnect_widget",
        "AVS:AVSconnect_widget#AVS_Widget_Types",
        "AVS:AVScorout_event_wait",
        "AVS:AVScorout_exec",
        "AVS:AVScorout_init",
        "AVS:AVScorout_input",
        "AVS:AVScorout_mark_changed",
        "AVS:AVScorout_output",
        "AVS:AVScorout_set_sync",
        "AVS:AVScorout_wait",
        "AVS:AVScorout_wait,",
        "AVS:AVScorout_X_wait",
        "AVS:AVScreate_input_port",
        "AVS:AVScreate_input_port#Description",
        "AVS:AVScreate_output_port",
        "AVS:AVSdata_alloc",
        "AVS:AVSdata_free",
        "AVS:AVSfield_alloc",
        "AVS:AVSfield_copy_points",
        "AVS:AVSFIELD_DATA_OFFSET",
        "AVS:AVSfield_data_ptr",
        "AVS:AVSfield_free",
        "AVS:AVSfield_get_dimensions",
        "AVS:AVSfield_get_extent",
        "AVS:AVSfield_get_int",
        "AVS:AVSfield_get_label",
        "AVS:AVSfield_get_labels",
        "AVS:AVSfield_get_mesh_id",
        "AVS:AVSfield_get_minmax",
        "AVS:AVSfield_get_unit",
        "AVS:AVSfield_get_units",
        "AVS:AVSfield_invalid_minmax",
        "AVS:AVSfield_make_template",
        "AVS:AVSFIELD_POINTS_OFFSET",
        "AVS:AVSfield_points_ptr",
        "AVS:AVSfield_reset_minmax",
        "AVS:AVSfield_set_extent",
        "AVS:AVSfield_set_int",
        "AVS:AVSfield_set_labels",
        "AVS:AVSfield_set_mesh_id",
        "AVS:AVSfield_set_minmax",
        "AVS:AVSfield_set_units",
        "AVS:AVSget_unique_id",
        "AVS:AVSinit_from_module_list",
        "AVS:AVSinit_modules",
        "AVS:AVSinitialize_output",
        "AVS:AVSinput_changed",
        "AVS:AVSload_user_data_types",
        "AVS:AVSmark_output_unchanged",
        "AVS:AVSmessage",
        "AVS:AVSmodify_float_parameter",
        "AVS:AVSmodify_parameter",
        "AVS:AVSmodify_parameter_prop",
        "AVS:AVSmodule_from_desc",
        "AVS:AVSmodule_status",
        "AVS:AVSparameter_changed",
        "AVS:AVSparameter_visible",
        "AVS:AVSPORT_FIELD",
        "AVS:AVSset_compute_proc",
        "AVS:AVSset_destroy_proc",
        "AVS:AVSset_init_proc",
        "AVS:AVSset_input_class",
        "AVS:AVSset_module_flags",
        "AVS:AVSset_module_name",
        "AVS:AVSset_output_class",
        "AVS:AVSset_output_flags",
        "AVS:AVSset_parameter_class",
        "AVS:AVSudata_get_double",
        "AVS:AVSudata_get_int",
        "AVS:AVSudata_get_real",
        "AVS:AVSudata_get_string",
        "AVS:AVSudata_set_double",
        "AVS:AVSudata_set_int",
        "AVS:AVSudata_set_real",
        "AVS:AVSudata_set_string",
        "AVS:Building_AVS_on_VMS",
        "AVS:Dev_Docs",
        "AVS:Mod_Docs",
        "AVS:Net_State",
        "Avs_blog:new_avs_development_and_release_structure",
        "AVS_Docs")

    private def fixOne(text: String, pat: String): String = {
        val fullPat = "href=\"" + pat + "\""
        ???
    }

    private def fixit(file: File): Unit = {
        var text = new String(Utility.readBinFile(file))
        patList.map(r => text = fixOne(text, r))
        //  lineList.map(line =>     text.replaceAll(".*href=\"", "").replaceAll("\".*", "")
    }

    private def isGoodPat(pat: String): Boolean = {

        val fp = "[^a-zA-Z0-9_]"
        val htmlFileName = pat.replaceAll(fp, ".") + ".html"
        val ok = !fileNameList.filter(fn => fn.replaceAll(fp, ".").equals(htmlFileName)).isEmpty
        ok
    }

    /**
     * For testing and development only.
     */
    def main(args: Array[String]): Unit = {
        println("Starting")
        val start = System.currentTimeMillis

        try {

            val goodPatList = patList.filter(pat => isGoodPat(pat))

            println("goodPatList:")
            goodPatList.map(gp => println("    " + gp))

            // fileList.map(f => fixit(f))

        }
        catch {
            case t: Throwable => t.printStackTrace
        }
        println("\nExiting.  Elapsed ms: " + (System.currentTimeMillis - start))
        System.exit(0)
    }

}