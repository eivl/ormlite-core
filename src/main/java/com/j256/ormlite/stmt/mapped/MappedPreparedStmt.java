package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement used by the {@link StatementBuilder#prepareQuery()} method.
 * 
 * @author graywatson
 */
public class MappedPreparedStmt<T> extends BaseMappedQuery<T> implements PreparedQuery<T>, PreparedDelete<T>,
		PreparedUpdate<T> {

	private final SelectArg[] selectArgs;
	private final Integer limit;
	private final StatementType type;

	public MappedPreparedStmt(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList,
			List<FieldType> resultFieldTypeList, List<SelectArg> selectArgList, Integer limit, StatementType type) {
		super(tableInfo, statement, argFieldTypeList, resultFieldTypeList);
		this.selectArgs = selectArgList.toArray(new SelectArg[selectArgList.size()]);
		// select args should match the field-type list
		if (argSqlTypes == null || selectArgs.length != argSqlTypes.length) {
			throw new IllegalArgumentException("Should be the same number of SelectArg and field-types in the arrays");
		}
		// this is an Integer because it may be null
		this.limit = limit;
		this.type = type;
	}

	public CompiledStatement compile(DatabaseConnection databaseConnection) throws SQLException {
		CompiledStatement stmt = databaseConnection.compileStatement(statement, type);
		if (limit != null) {
			stmt.setMaxRows(limit);
		}
		// set any arguments if there are any selectArgs
		Object[] args = null;
		if (logger.isTraceEnabled() && selectArgs.length > 0) {
			args = new Object[selectArgs.length];
		}
		for (int i = 0; i < selectArgs.length; i++) {
			Object arg = selectArgs[i].getValue();
			// sql statement arguments start at 1
			if (arg == null) {
				stmt.setNull(i + 1, argSqlTypes[i]);
			} else {
				stmt.setObject(i + 1, arg, argSqlTypes[i]);
			}
			if (args != null) {
				args[i] = arg;
			}
		}
		logger.debug("prepared statement '{}' with {} args", statement, selectArgs.length);
		if (args != null) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("prepared statement arguments: {}", (Object) args);
		}
		return stmt;
	}

	public String getStatement() {
		return statement;
	}
}
